package com.tom.cpm.shared.parts.anim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tom.cpm.shared.animation.AnimationNew;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.IAnimation;
import com.tom.cpm.shared.animation.StagedAnimation;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.IResolvedModelPart;
import com.tom.cpm.shared.parts.ModelPartType;

public class ModelPartAnimationNew implements IModelPart, IResolvedModelPart {
	private AnimLoaderState state = new AnimLoaderState();

	public ModelPartAnimationNew(IOHelper din, ModelDefinition def) throws IOException {
		while (din.readObjectBlock(TagType.VALUES, TagType.read(state)) != TagType.END);
		state.processTriggers();
	}

	public ModelPartAnimationNew(Editor e) {
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
		dout.writeEnum(TagType.END);
		dout.writeVarInt(0);
	}

	@Override
	public void apply(ModelDefinition def) {
		AnimationRegistry reg = def.getAnimations();
		Map<SerializedTrigger, List<IAnimation>> anims = new HashMap<>();
		Map<Integer, IAnimation> convAnims = new HashMap<>();
		Map<SerializedTrigger, Integer> trigIds = new HashMap<>();
		state.getAnims().forEach((id, an) -> {
			SerializedTrigger tr = state.getTriggers().get(an.triggerID);
			if (tr != null) {
				trigIds.put(tr, an.triggerID);
				AnimationNew a = new AnimationNew(an.priority, an.duration);
				an.animatorChannels.values().forEach(ac -> ac.addToAnim(a, def));
				anims.computeIfAbsent(tr, __ -> new ArrayList<>()).add(a);
				convAnims.put(id, a);
			}
		});
		Map<IAnimation, StagedAnimation> st = new HashMap<>();
		Map<SerializedTrigger, StagedAnimation> st2 = new HashMap<>();
		anims.forEach((tr, ans) -> {
			if (tr.anim != null && tr.anim.isStaged()) {
				if (tr.animID != -1) {
					IAnimation an = convAnims.get(tr.animID);
					StagedAnimation san = st.computeIfAbsent(an, __ -> {
						StagedAnimation sa = new StagedAnimation();
						sa.addPlay(an);
						return sa;
					});
					if (tr.anim == AnimationType.SETUP)ans.forEach(san::addPre);
					else if (tr.anim == AnimationType.FINISH)ans.forEach(san::addPost);
				} else if (tr.triggerID != -1) {
					SerializedTrigger bt = state.getTriggers().get(tr.triggerID);
					List<IAnimation> an = anims.getOrDefault(bt, Collections.emptyList());
					if (!an.isEmpty()) {
						StagedAnimation san = st2.computeIfAbsent(bt, __ -> {
							StagedAnimation sa = new StagedAnimation();
							an.forEach(sa::addPlay);
							return sa;
						});
						if (tr.anim == AnimationType.SETUP)ans.forEach(san::addPre);
						else if (tr.anim == AnimationType.FINISH)ans.forEach(san::addPost);
					}
				}
			}
		});
		//TODO sorting won't be necessary after full trigger rewrite
		anims.entrySet().stream().sorted(Comparator.comparingInt(e -> trigIds.get(e.getKey()))).forEach((e) -> {
			SerializedTrigger a = e.getKey();
			List<IAnimation> b = e.getValue();
			StagedAnimation sa2 = st2.get(a);
			a.registerOld(reg, (sa2 != null ? sa2.getAll() : b).stream().flatMap(an -> {
				StagedAnimation sa = st.get(an);
				if (sa != null)return sa.getAll().stream();
				else return Stream.of(an);
			}).collect(Collectors.toList()));
		});
		state.applyInfos(reg);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.ANIMATION_NEW;
	}

	@Override
	public String toString() {
		/*StringBuilder sb = new StringBuilder("AnimationsNew:\n\tTriggers:");
		for (int i = 0;i<state.getTriggers().size();i++) {
			SerializedTrigger st = state.getTriggers().get(i);
			sb.append("\n\t\t");
			sb.append(i);
			sb.append(": ");
			sb.append(st.toString().replace("\n", "\n\t\t"));
		}
		sb.append("\n\tAnimations:");
		for (int i = 0;i<state.getAnims().size();i++) {
			SerializedAnimation sa = state.getAnims().get(i);
			sb.append("\n\t\t");
			sb.append(i);
			sb.append(": ");
			sb.append(sa.toString().replace("\n", "\n\t\t"));
		}*/
		StringBuilder sb = new StringBuilder("AnimationsNew:\n\tAnimations: ");
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
		return sb.toString();
	}
}
