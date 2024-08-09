package com.tom.cpm.shared.parts.anim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.AnimationTrigger;
import com.tom.cpm.shared.animation.IAnimation;
import com.tom.cpm.shared.animation.StagedAnimation;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.IResolvedModelPart;
import com.tom.cpm.shared.parts.ModelPartType;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;

public class ModelPartAnimationNew implements IModelPart, IResolvedModelPart {
	private AnimLoaderState state;

	public ModelPartAnimationNew(IOHelper din, ModelDefinition def) throws IOException {
		state = new AnimLoaderState(def);
		while (din.readObjectBlock(TagType.VALUES, TagType.read(state)) != TagType.END);
		state.processTriggers();
	}

	public ModelPartAnimationNew(Editor e) {
		state = new AnimLoaderState(e.definition);
		state.loadFromEditor(e);
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		state.writeInfo(dout);
		for (int i = 0;i<state.getTriggers().size();i++) {
			SerializedTrigger st = state.getTriggers().get(i);
			st.write(dout);
		}
		for (int i = 0;i<state.getAnims().size();i++) {
			SerializedAnimation sa = state.getAnims().get(i);
			sa.write(dout);
		}
		for (int i = 0;i<state.getGestureButtons().size();i++) {
			AbstractGestureButtonData gb = state.getGestureButtons().get(i);
			try (IOHelper d = dout.writeNextObjectBlock(TagType.GESTURE_BUTTON)) {
				d.writeEnum(gb.getType());
				gb.write(d);
			}
		}
		for (int i = 0;i<state.getStagedList().size();i++) {
			int id = state.getStagedList().get(i);
			try (IOHelper d = dout.writeNextObjectBlock(TagType.INIT_STAGED_ANIM)) {
				d.writeVarInt(id);
			}
		}
		dout.writeEnum(TagType.END);
		dout.writeVarInt(0);
	}

	@Override
	public void apply(ModelDefinition def) {
		AnimationRegistry reg = def.getAnimations();
		state.getGestureButtons().forEach(reg::register);
		state.applyInfos(reg);
		Map<SerializedTrigger, List<IAnimation>> anims = new HashMap<>();
		state.getAnims().forEach((id, an) -> {
			SerializedTrigger tr = state.getTriggers().get(an.triggerID);
			if (tr != null) {
				IAnimation a = an.compile(def);
				anims.computeIfAbsent(tr, __ -> new ArrayList<>()).add(a);
			}
		});
		for (int i = 0; i < state.getStagedList().size(); i++) {
			int tId = state.getStagedList().get(i);
			StagedAnimation an = new StagedAnimation();
			final int fi = i;
			anims.entrySet().stream().filter(e -> e.getKey().stagingID == fi && e.getKey().stage != null).forEach(e -> {
				AnimationTrigger trigger = e.getKey().compileStaging(e.getValue());
				switch(e.getKey().stage) {
				case FINISH:
					an.addPost(trigger);
					break;
				case PLAY:
					an.addPlay(trigger);
					break;
				case SETUP:
					an.addPre(trigger);
					break;
				default:
					break;
				}
			});
			SerializedTrigger tr = this.state.getTriggers().get(tId);
			anims.computeIfAbsent(tr, __ -> new ArrayList<>()).addAll(an.getAll());
		}
		for (Entry<SerializedTrigger, List<IAnimation>> e : anims.entrySet()) {
			AnimationTrigger trigger = e.getKey().compile(reg, e.getValue());
			if (trigger != null)
				reg.register(trigger);
		}
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.ANIMATION_NEW;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("AnimationsNew:\n\tTriggers:");
		for (int i = 0;i<state.getAnims().size();i++) {
			SerializedAnimation sa = state.getAnims().get(i);
			sb.append("\n\t\t");
			sb.append(i);
			sb.append(": ");
			sb.append("Animation: ");
			sb.append(sa.triggerID);
			sb.append(": ");
			sb.append(String.valueOf(state.getTriggers().get(sa.triggerID)).replace("\n", "\n\t\t"));
			sb.append("\n\t\t\t");
			sb.append(sa.toString());
		}
		sb.append("\n\tParameters\n\t\t");
		sb.append(state.getParameters());
		sb.append("\n\tButtons:");
		for (int i = 0;i<state.getGestureButtons().size();i++) {
			AbstractGestureButtonData gb = state.getGestureButtons().get(i);
			sb.append("\n\t\t");
			sb.append(i);
			sb.append(": ");
			sb.append(gb.getType().name());
		}
		return sb.toString();
	}
}
