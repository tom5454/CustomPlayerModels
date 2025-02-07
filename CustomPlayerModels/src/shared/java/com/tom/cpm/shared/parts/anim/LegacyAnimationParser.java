package com.tom.cpm.shared.parts.anim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tom.cpm.shared.animation.Animation;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.AnimationTrigger;
import com.tom.cpm.shared.animation.AnimationTrigger.GestureTrigger;
import com.tom.cpm.shared.animation.AnimationTrigger.LayerTrigger;
import com.tom.cpm.shared.animation.AnimationTrigger.ValueTrigger;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IAnimation;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.StagedAnimation;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.parts.anim.menu.BoolParameterToggleButtonData;
import com.tom.cpm.shared.parts.anim.menu.CustomPoseGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.LegacyDropdownButtonData;
import com.tom.cpm.shared.parts.anim.menu.ValueParameterButtonData;

public class LegacyAnimationParser {
	private final ModelDefinition def;
	private final AnimationRegistry reg;
	private Map<Gesture, Integer> layerToId = new HashMap<>();
	private Map<CustomPose, Integer> poseGid = new HashMap<>();
	private List<Gesture> stageGestures = new ArrayList<>();
	public Map<String, List<IAnimation>> gestures = new HashMap<>();
	private Map<IPose, List<IAnimation>> animations = new HashMap<>();
	private Map<String, Gesture> gesturesMap = new HashMap<>();
	private Map<String, Pose> customPoses = new HashMap<>();
	private Set<IPose> mustFinishPoses = new HashSet<>();

	public LegacyAnimationParser(ModelDefinition def) {
		this.def = def;
		this.reg = def.getAnimations();
	}

	private static class Group implements IPoseGesture {
		private List<Gesture> gs = new ArrayList<>();
		private String id;
		private int order = Integer.MIN_VALUE;

		public Group(String id) {
			this.id = id;
		}

		public void add(Gesture g) {
			gs.add(g);
		}

		@Override
		public int getOrder() {
			if (order == Integer.MIN_VALUE)
				order = gs.stream().mapToInt(Gesture::getOrder).max().orElse(0);

			return order;
		}

		@Override
		public void register(LegacyAnimationParser reg) {
			LegacyDropdownButtonData dt = new LegacyDropdownButtonData();
			dt.setName(id);
			dt.command = gs.stream().anyMatch(g -> g.command);
			dt.isProperty = gs.stream().anyMatch(g -> g.isProperty);
			dt.setDef(reg.def);
			for (Gesture g : gs) {
				int l = reg.layerToId.get(g);
				reg.reg.register(new LayerTrigger(reg.reg, Collections.singleton(VanillaPose.GLOBAL), g.animation, l, 1, true, g.mustFinish));
				dt.register(g.name, l);
			}
			reg.reg.register(dt);
		}
	}

	private static interface IPoseGesture {
		int getOrder();
		void register(LegacyAnimationParser state);
	}

	public static class Pose implements IPoseGesture {
		private CustomPose pose;
		public boolean hidden;

		public Pose(CustomPose pose) {
			this.pose = pose;
		}

		@Override
		public int getOrder() {
			return pose.order;
		}

		@Override
		public void register(LegacyAnimationParser reg) {
			CustomPoseGestureButtonData data = new CustomPoseGestureButtonData(true);
			data.setName(pose.getId());
			data.layerCtrl = pose.layerCtrl;
			data.command = pose.command;
			data.hidden = hidden;
			int id = reg.poseGid.get(pose);
			if(pose.layerCtrl)data.gid = id;
			data.id = id;
			data.setDef(reg.def);
			reg.reg.register(data);
		}
	}

	public static class Gesture implements IPoseGesture {
		public final AnimationType type;
		public List<IAnimation> animation;
		public boolean isLoop;
		public String name;
		public byte defVal;
		public int order;
		public boolean isProperty, command, layerCtrl, mustFinish;
		public String group;
		public int gid;
		public byte maxValue;
		public boolean interpolateVal;
		public boolean hidden;

		public Gesture(AnimationType type, List<IAnimation> animation, String name, boolean isLoop, int order, boolean mustFinish) {
			this.type = type;
			this.animation = animation;
			this.name = name;
			this.isLoop = isLoop;
			this.order = order;
			this.mustFinish = mustFinish;
		}

		@Override
		public int getOrder() {
			return order;
		}

		@Override
		public void register(LegacyAnimationParser reg) {
			switch (type) {
			case GESTURE:
			{
				CustomPoseGestureButtonData data = new CustomPoseGestureButtonData(false);
				data.setName(name);
				data.layerCtrl = layerCtrl;
				data.command = command;
				data.id = gid;
				data.hidden = hidden;
				if (layerCtrl)data.gid = gid;
				if (!isLoop) {
					int len = animation.stream().mapToInt(a -> a.getDuration(AnimationMode.PLAYER)).max().orElse(-1);
					data.gestureTimeout = len == -1 ? -1 : ((int) Math.ceil(len / 50f) + 5);
				}
				data.setDef(reg.def);
				reg.reg.register(data);
				reg.reg.register(new GestureTrigger(reg.reg, Collections.singleton(VanillaPose.GLOBAL), animation, gid, gid, isLoop, mustFinish));
			}
			break;

			case LAYER:
			{
				BoolParameterToggleButtonData dt = new BoolParameterToggleButtonData();
				dt.setName(name);
				dt.command = command;
				dt.isProperty = isProperty;
				dt.parameter = reg.layerToId.get(this);
				dt.mask = 1;
				dt.hidden = hidden;
				dt.setDef(reg.def);
				reg.reg.register(dt);
				reg.reg.register(new LayerTrigger(reg.reg, Collections.singleton(VanillaPose.GLOBAL), animation, dt.parameter, 1, true, mustFinish));
			}
			break;

			case VALUE_LAYER:
			{
				ValueParameterButtonData dt = new ValueParameterButtonData();
				dt.setName(name);
				dt.command = command;
				dt.isProperty = isProperty;
				dt.parameter = reg.layerToId.get(this);
				dt.maxValue = maxValue == 0 ? 255 : Byte.toUnsignedInt(maxValue);
				dt.hidden = hidden;
				dt.setDef(reg.def);
				reg.reg.register(dt);
				reg.reg.register(new ValueTrigger(reg.reg, Collections.singleton(VanillaPose.GLOBAL), animation, dt.parameter, maxValue == 0 ? true : interpolateVal));
			}
			break;

			default:
				break;
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void finishLoading() {
		Map<String, StagedAnimation> anims = new HashMap<>();
		for (Gesture g : stageGestures) {
			String[] nm = g.name.split(":", 2);
			if(nm.length == 2) {
				IPose pose = null;
				StagedAnimation san = anims.computeIfAbsent(g.name, k -> new StagedAnimation());
				if(g.type == AnimationType.SETUP)g.animation.forEach(san::addPre);
				else if(g.type == AnimationType.FINISH)g.animation.forEach(san::addPost);
				switch (nm[0]) {
				case "p":
					for(VanillaPose p : VanillaPose.VALUES) {
						if(nm[1].equals(p.name().toLowerCase(Locale.ROOT))) {
							pose = p;
							break;
						}
					}
					//Fall through
				case "c":
					if(pose == null) {
						Pose p = customPoses.get(nm[1]);
						if (p != null)pose = p.pose;
					}
					if (pose != null) {
						animations.computeIfPresent(pose, (p, an) -> {
							boolean mf = mustFinishPoses.contains(p);
							an.forEach(a -> san.addPlay(a, mf));
							if (mf)mustFinishPoses.remove(p);//Disable must finish as the staged handler will apply it
							return san.getAll();
						});
					}
					break;

				case "g":
					gesturesMap.computeIfPresent(nm[1], (k, gs) -> {
						gs.animation.forEach(a -> san.addPlay(a, gs.mustFinish));
						gs.animation = san.getAll();
						gs.mustFinish = false;//Disable must finish as the staged handler will apply it
						return gs;
					});
					break;

				default:
					break;
				}
			}
		}
	}

	public void addPose(IPose pose, Animation anim, boolean finish) {
		animations.computeIfAbsent(pose, __ -> new ArrayList<>()).add(anim);
		if (finish)mustFinishPoses.add(pose);
	}

	public void addCustomPose(Pose pose, int gid) {
		customPoses.put(pose.pose.getName(), pose);
		if(gid != -1)
			poseGid.put(pose.pose, gid);
	}

	public void addGesture(Gesture g, int gid) {
		if (g.type.isStaged()) {
			stageGestures.add(g);
		} else {
			gesturesMap.put(g.name, g);
			if(g.type.isLayer())
				layerToId.put(g, layerToId.size() + 2);
			if(gid != -1)
				g.gid = gid;
		}
	}

	public void register() {
		finishLoading();
		Map<String, Group> groups = new HashMap<>();

		Stream.concat(gesturesMap.values().stream(), customPoses.values().stream()).
		sorted(Comparator.comparingInt(IPoseGesture::getOrder)).
		map(g -> {
			if (g instanceof Gesture && ((Gesture)g).group != null) {
				String k = ((Gesture)g).group;
				Group gr = groups.get(k);
				if (gr == null) {
					gr = new Group(k);
					groups.put(k, gr);
					gr.add((Gesture) g);
					return gr;
				}
				gr.add((Gesture) g);
				return null;
			} else {
				return g;
			}
		}).filter(e -> e != null).collect(Collectors.toList()).
		forEach(g -> g.register(this));
		byte[] sync = new byte[layerToId.size() + 2];
		layerToId.forEach((g, i) -> sync[i] = g.defVal);
		reg.setParams(new ParameterDetails(sync, new byte[0]));
		animations.forEach((p, an) -> {
			AnimationTrigger t = new AnimationTrigger(reg, Collections.singleton(p), p instanceof VanillaPose ? (VanillaPose) p : null, an, true, mustFinishPoses.contains(p));
			reg.register(t);
		});
	}
}
