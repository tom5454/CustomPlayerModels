package com.tom.cpm.shared.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.InterpolatorChannel;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.anim.IElem;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.parts.anim.AnimLoaderState;
import com.tom.cpm.shared.parts.anim.AnimatorChannel;
import com.tom.cpm.shared.parts.anim.ConstantTimeBool;
import com.tom.cpm.shared.parts.anim.ConstantTimeFloat;
import com.tom.cpm.shared.parts.anim.ParameterDetails.ParameterAllocator;
import com.tom.cpm.shared.parts.anim.ParameterDetails.ParameterAllocator.BitInfo;
import com.tom.cpm.shared.parts.anim.SerializedAnimation;
import com.tom.cpm.shared.parts.anim.SerializedTrigger;
import com.tom.cpm.shared.parts.anim.StageType;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData.AbstractCommandTriggerableData;
import com.tom.cpm.shared.parts.anim.menu.BoolParameterToggleButtonData;
import com.tom.cpm.shared.parts.anim.menu.CustomPoseGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.DropdownButtonData;
import com.tom.cpm.shared.parts.anim.menu.ValueParameterButtonData;

public class AnimationExporter {
	public final Editor editor;
	public final AnimLoaderState an;

	public AnimationExporter(Editor editor, AnimLoaderState an) {
		this.editor = editor;
		this.an = an;
	}

	public ParameterAllocator paramAlloc = new ParameterAllocator();
	public List<Pair<Integer, AbstractGestureButtonData>> buttons = new ArrayList<>();
	public Map<String, ParameterInfo> allButtons = new HashMap<>();
	public Set<ModelElement> allElems = new HashSet<>();
	public Map<SerializedTrigger, Integer> triggers = new HashMap<>();
	public Map<EditorAnim, Integer> animTriggers = new HashMap<>();
	public Map<EditorAnim, SerializedAnimation> anims = new HashMap<>();
	public Map<EditorAnim, Staging> stagingAnimMap = new HashMap<>();
	public List<Staging> stagingAnimList = new ArrayList<>();

	public void processElements() {
		Editor.walkElements(editor.elements, allElems::add);
	}

	public void processAnimation(EditorAnim a) {
		SerializedTrigger tr = new SerializedTrigger();
		if (a.pose instanceof VanillaPose) {
			tr.pose = (VanillaPose) a.pose;
			tr.mustFinish = a.mustFinish;
		} else if(a.type == AnimationType.CUSTOM_POSE || a.type == AnimationType.GESTURE || a.type.isLayer()) {
			tr.looping = a.type == AnimationType.GESTURE ? a.loop : true;
			if (!allButtons.containsKey(a.getId())) {
				ParameterInfo info = makeButtonInfo(a);
				if (!a.type.isLayer())
					info.button.layerCtrl = a.layerControlled;
				info.button.command = a.command;
				info.button.isProperty = a.isProperty;
				info.apply(tr);
				if (a.type == AnimationType.GESTURE && !a.loop) {
					int len = editor.animations.stream().mapToInt(ax -> ax.duration).max().orElse(-1);
					if (len > 0)
						((CustomPoseGestureButtonData) info.button).gestureTimeout = ((int) Math.ceil(len / 50f) + 5);
				}
			} else {
				allButtons.get(a.getId()).apply(tr);
			}
			tr.anim = a.type;
			tr.mustFinish = a.mustFinish;
		} else if(a.type.isStaged()) {
			List<EditorAnim> l = a.findLinkedAnims().collect(Collectors.toList());
			Staging st = l.stream().map(stagingAnimMap::get).filter(e -> e != null).findFirst().orElseGet(() -> {
				Staging s = new Staging();
				s.id = stagingAnimList.size();
				stagingAnimList.add(s);
				stagingAnimMap.put(a, s);
				return s;
			});
			l.forEach(ea -> stagingAnimMap.put(ea, st));
			stagingAnimMap.put(a, st);
			st.play.addAll(l);

			if (a.type == AnimationType.SETUP)st.setup.add(a);
			else st.finish.add(a);

			tr.stage = a.type == AnimationType.SETUP ? StageType.SETUP : StageType.FINISH;
			tr.stagingID = st.id;
		}
		int id = triggers.computeIfAbsent(tr, an::newTrigger);
		animTriggers.put(a, id);
		SerializedAnimation anim = new SerializedAnimation();
		anims.put(a, anim);
		anim.triggerID = id;
		anim.priority = a.priority;
		anim.duration = a.pose instanceof VanillaPose && ((VanillaPose)a.pose).hasStateGetter() || a.type == AnimationType.VALUE_LAYER ? VanillaPose.DYNAMIC_DURATION_DIV : a.duration;
		an.newAnimation(anim);
		List<ModelElement> elems = a.getComponentsFiltered();
		List<AnimFrame> frames = a.getFrames();
		elems.forEach(me -> {
			if(!allElems.contains(me))return;
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
	}

	private static class Staging {
		public int id;
		public Set<EditorAnim> setup = new HashSet<>();
		public Set<EditorAnim> play = new HashSet<>();
		public Set<EditorAnim> finish = new HashSet<>();
		public int triggerId;
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

	public List<AbstractGestureButtonData> sortButtons() {
		return buttons.stream().sorted(Comparator.comparingInt(Pair::getKey)).map(Pair::getValue).collect(Collectors.toList());
	}

	public void linkStagingAnims() {
		stagingAnimList.forEach(s -> {
			SerializedTrigger tr = new SerializedTrigger();
			tr.stagingID = s.id;
			tr.stage = StageType.PLAY;
			tr.mustFinish = s.play.stream().anyMatch(e -> e.mustFinish);
			int id = triggers.computeIfAbsent(tr, an::newTrigger);

			List<SerializedTrigger> trigs = s.play.stream().map(animTriggers::get).map(an.getTriggers()::get).filter(e -> e != null).distinct().collect(Collectors.toList());
			s.play.stream().map(anims::get).filter(e -> e != null).forEach(a -> {
				a.triggerID = id;
			});
			if (trigs.size() == 1) {
				s.triggerId = triggers.get(trigs.get(0));
				trigs.get(0).mustFinish = false;//TODO fix later
			} else {
				throw new RuntimeException("Multiple triggers not supported");
			}
			an.getStagedList().add(s.triggerId);
		});
	}

	private ParameterInfo makeButtonInfo(EditorAnim a) {
		switch (a.type) {
		case CUSTOM_POSE:
		case GESTURE:
		{
			CustomPoseGestureButtonData dt = new CustomPoseGestureButtonData(a.type == AnimationType.CUSTOM_POSE);
			dt.setName(a.getId());
			dt.id = a.type == AnimationType.CUSTOM_POSE ? paramAlloc.newPose(a.getId()) : paramAlloc.newGesture(a.getId());
			dt.layerCtrl = a.layerControlled;
			ParameterInfo param = new ParameterInfo(dt, dt.id);
			allButtons.put(a.getId(), param);
			buttons.add(Pair.of(a.order, dt));
			return param;
		}
		case LAYER:
		{
			if (a.group != null) {
				if (!allButtons.containsKey(a.group)) {
					DropdownButtonData dt = new DropdownButtonData();
					dt.parameter = paramAlloc.allocByteSync(a.group, (byte) 0);
					dt.setName(a.group);
					dt.add("");//None option injected
					ParameterInfo param = new ParameterInfo(dt, dt.parameter, 0, false);
					allButtons.put(a.group, param);
					buttons.add(Pair.of(a.order, dt));
				}
				ParameterInfo info = allButtons.get(a.group);
				if (!(info.button instanceof DropdownButtonData)) {
					throw new RuntimeException("Animation name conflict " + a.group);//TODO localize
				}
				DropdownButtonData dt = (DropdownButtonData) info.button;
				ParameterInfo pr = new ParameterInfo(dt, dt.parameter, dt.add(a.getId()), false);
				allButtons.put(a.getId(), pr);
				return pr;
			} else {
				BoolParameterToggleButtonData dt = new BoolParameterToggleButtonData();
				BitInfo bit = paramAlloc.allocBitSync(a.getId(), a.layerDefault > 0.5f);
				dt.setInfo(bit);
				dt.setName(a.getId());
				ParameterInfo param = new ParameterInfo(dt, bit);
				allButtons.put(a.getId(), param);
				buttons.add(Pair.of(a.order, dt));
				return param;
			}
		}
		case VALUE_LAYER:
		{
			ValueParameterButtonData dt = new ValueParameterButtonData();
			dt.setName(a.getId());
			dt.parameter = paramAlloc.allocByteSync(a.getId(), (byte) (a.layerDefault * a.maxValue));
			dt.maxValue = a.maxValue;
			ParameterInfo param = new ParameterInfo(dt, dt.parameter, a.interpolateValue);
			allButtons.put(a.getId(), param);
			buttons.add(Pair.of(a.order, dt));
			return param;
		}
		default:
			break;
		}
		return null;
	}

	public static class ParameterInfo {
		public AbstractCommandTriggerableData button;
		public int parameter;
		public int value;
		public boolean bitMask;
		public boolean interpolate;

		public ParameterInfo(AbstractCommandTriggerableData button, int id) {
			this.button = button;
			this.value = id;
		}

		public ParameterInfo(AbstractCommandTriggerableData button, int parameter, boolean interpolate) {
			this.button = button;
			this.parameter = parameter;
			this.interpolate = interpolate;
		}

		public ParameterInfo(AbstractCommandTriggerableData button, int parameter, int value, boolean bitMask) {
			this.button = button;
			this.parameter = parameter;
			this.value = value;
			this.bitMask = bitMask;
		}

		public ParameterInfo(AbstractCommandTriggerableData button, BitInfo bit) {
			this(button, bit.param, bit.mask, true);
		}

		public void apply(SerializedTrigger st) {
			st.parameter = parameter;
			st.value = value;
			st.bitMask = bitMask;
			st.parameterInterpolate = interpolate;
		}
	}
}
