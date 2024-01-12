package com.tom.cpm.shared.parts.anim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.InterpolatorChannel;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.anim.IElem;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;
import com.tom.cpm.shared.io.IOHelper;

public class AnimLoaderState {
	private Map<Integer, SerializedAnimation> anims = new HashMap<>();
	private Map<Integer, SerializedTrigger> triggers = new HashMap<>();
	private int trID = 0;
	private int anID = 0;
	private SerializedAnimation cA = null;
	private SerializedTrigger cT = null;
	private int blankId, resetId;
	private String modelProfilesId;
	private int defGidMask, valGidMask;
	private List<PlayerSkinLayer> allLayers;

	public SerializedTrigger getTrigger() {
		return cT;
	}

	public SerializedAnimation getAnim() {
		return cA;
	}

	public int newTrigger(SerializedTrigger tr) {
		int id = trID++;
		triggers.put(id, tr);
		cT = tr;
		return id;
	}

	public int newAnimation(SerializedAnimation an) {
		int id = anID++;
		anims.put(id, an);
		cA = an;
		return id;
	}

	public Map<Integer, SerializedAnimation> getAnims() {
		return anims;
	}

	public Map<Integer, SerializedTrigger> getTriggers() {
		return triggers;
	}

	private void loadInfo(Editor e) {
		valGidMask = e.animEnc == null ? 0 :  PlayerSkinLayer.encode(e.animEnc.freeLayers);
		defGidMask = e.animEnc == null ? 0 : (PlayerSkinLayer.encode(e.animEnc.defaultLayerValue) & (~valGidMask));
		resetId = defGidMask | valGidMask;
		blankId = defGidMask | 0;
		modelProfilesId = e.modelId;
		allLayers = e.animEnc != null ? new ArrayList<>(e.animEnc.freeLayers) : new ArrayList<>();
		Collections.sort(allLayers);
	}

	public void loadFromEditor(Editor e) {
		loadInfo(e);
		Map<SerializedTrigger, Integer> triggers = new HashMap<>();
		Map<EditorAnim, SerializedTrigger> stagingIds = new HashMap<>();
		Map<EditorAnim, Integer> animTriggers = new HashMap<>();
		e.animations.forEach(a -> {
			SerializedTrigger tr = new SerializedTrigger();
			if (a.pose instanceof VanillaPose)tr.pose = (VanillaPose) a.pose;
			else if(a.type == AnimationType.CUSTOM_POSE || a.type == AnimationType.GESTURE || a.type.isLayer()) {
				tr.name = a.getId();
				tr.anim = a.type;
				tr.group = a.group;
				tr.layerCtrl = a.layerControlled;
				tr.command = a.command;
				tr.isProperty = a.isProperty;
				tr.order = a.order;
				tr.defaultValue = (byte) (a.layerDefault * 0xff);
				tr.looping = a.loop;
			} else if(a.type.isStaged()) {
				tr.anim = a.type;
				tr.triggerID = stagingIds.size();
				stagingIds.put(a, tr);
			}
			int id = triggers.computeIfAbsent(tr, this::newTrigger);
			animTriggers.put(a, id);
			SerializedAnimation anim = new SerializedAnimation();
			anim.triggerID = id;
			anim.priority = a.priority;
			anim.duration = a.duration;
			newAnimation(anim);
			List<ModelElement> elems = a.getComponentsFiltered();
			List<AnimFrame> frames = a.getFrames();
			elems.forEach(me -> {
				Map<InterpolatorChannel, Integer> c = AnimatorChannel.addCubeToChannels(anim, me.id, a.add);
				//TODO replace with include system later
				if (frames.stream().anyMatch(f -> f.hasPosChanges(me))) {
					addChannel(anim, c, me, InterpolatorChannel.POS_X, a, 0);
					addChannel(anim, c, me, InterpolatorChannel.POS_Y, a, 0);
					addChannel(anim, c, me, InterpolatorChannel.POS_Z, a, 0);
				}
				if (frames.stream().anyMatch(f -> f.hasRotChanges(me))) {
					addChannel(anim, c, me, InterpolatorChannel.ROT_X, a, 0);
					addChannel(anim, c, me, InterpolatorChannel.ROT_Y, a, 0);
					addChannel(anim, c, me, InterpolatorChannel.ROT_Z, a, 0);
				}
				if (frames.stream().anyMatch(f -> f.hasColorChanges(me))) {
					addChannel(anim, c, me, InterpolatorChannel.COLOR_R, a, 0);
					addChannel(anim, c, me, InterpolatorChannel.COLOR_G, a, 0);
					addChannel(anim, c, me, InterpolatorChannel.COLOR_B, a, 0);
				}
				if (frames.stream().anyMatch(f -> f.hasVisChanges(me))) {
					boolean[] array = new boolean[frames.size()];
					for (int i = 0; i < frames.size(); i++) {
						AnimFrame frm = frames.get(i);
						IElem dt = frm.getData(me);
						if(dt == null) array[i] = me.isVisible();
						else array[i] = dt.isVisible();
					}
					anim.animatorChannels.get(c.get(null)).frameData = new ConstantTimeBool(array);
				}
				if (frames.stream().anyMatch(f -> f.hasScaleChanges(me))) {
					addChannel(anim, c, me, InterpolatorChannel.SCALE_X, a, 1);
					addChannel(anim, c, me, InterpolatorChannel.SCALE_Y, a, 1);
					addChannel(anim, c, me, InterpolatorChannel.SCALE_Z, a, 1);
				}
			});
		});
		stagingIds.forEach((ea, t) -> {
			EditorAnim a = ea.findLinkedAnim();
			Integer id = animTriggers.get(a);
			if (id != null) {
				t.triggerID = id;
			}
		});
		handleLayers();
	}

	private static void addChannel(SerializedAnimation anim, Map<InterpolatorChannel, Integer> c, ModelElement elem, InterpolatorChannel chn, EditorAnim ea, float empty) {
		List<AnimFrame> frames = ea.getFrames();
		float[] array = new float[frames.size()];
		for (int i = 0; i < frames.size(); i++) {
			AnimFrame frm = frames.get(i);
			IElem dt = frm.getData(elem);
			if(dt == null) {
				if(ea.add)array[i] = empty;
				else array[i] = elem.part(chn);
			} else array[i] = dt.part(chn);
		}
		anim.animatorChannels.get(c.get(chn)).frameData = new ConstantTimeFloat(ea.intType, array);
	}

	public static void parseInfo(IOHelper block, AnimLoaderState state) throws IOException {
		state.blankId = block.read();
		state.resetId = block.read();
		state.modelProfilesId = block.readUTF();
	}

	public void writeInfo(IOHelper dout) throws IOException {
		try (IOHelper d = dout.writeNextObjectBlock(TagType.CONTROL_INFO)) {
			d.write(blankId);
			d.write(resetId);
			if (modelProfilesId == null)d.writeVarInt(0);
			else d.writeUTF(modelProfilesId);
		}
	}

	public void applyInfos(AnimationRegistry reg) {
		reg.setBlankGesture(blankId);
		reg.setPoseResetId(resetId);
		reg.setProfileId(modelProfilesId);
	}

	public void handleLayers() {
		SerializedTrigger.handleLayers(triggers, allLayers, blankId, resetId, valGidMask, defGidMask);
	}

	public void processTriggers() {
		SerializedTrigger.handleGestures(triggers, blankId, resetId);
	}
}
