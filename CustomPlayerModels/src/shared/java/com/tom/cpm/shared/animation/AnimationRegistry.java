package com.tom.cpm.shared.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tom.cpm.shared.model.CopyTransform;
import com.tom.cpm.shared.parts.anim.ParameterDetails;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.CommandAction;
import com.tom.cpm.shared.parts.anim.menu.CustomPoseGestureButtonData;

public class AnimationRegistry {
	private ParameterDetails params = ParameterDetails.DEFAULT;
	private List<AnimatedTexture> animatedTextures = new ArrayList<>();
	private List<AbstractGestureButtonData> namedActions = new ArrayList<>();
	private Map<String, AbstractGestureButtonData> namedActionByKeybind = new HashMap<>();
	private Map<String, CommandAction> commandActionsMap = new HashMap<>();
	private Map<IPose, List<AnimationTrigger>> poseToTriggers = new HashMap<>();
	private Set<AnimationTrigger> animations = new HashSet<>();
	private Map<Integer, IPose> poseById = new HashMap<>();
	private Map<Integer, CustomPoseGestureButtonData> gestureById = new HashMap<>();
	private Map<Integer, IPose> encodedToPose = new HashMap<>();
	private Map<Integer, CustomPoseGestureButtonData> encodedToGesture = new HashMap<>();
	private Map<String, CustomPose> customPoseByName = new HashMap<>();
	private List<CopyTransform> copyTransforms = new ArrayList<>();
	private int blankGesture;
	private int poseResetId;
	private String profileId;

	public Set<AnimationTrigger> getAnimations() {
		return animations;
	}

	public IPose getPoseEncoded(int gesture, IPose pose) {
		return encodedToPose.getOrDefault(gesture, pose);
	}

	public IPose getPoseById(int gesture, IPose pose) {
		return poseById.getOrDefault(gesture, pose);
	}

	public List<AnimationTrigger> getPoseAnimations(IPose id) {
		return poseToTriggers.getOrDefault(id, Collections.emptyList());
	}

	public CustomPoseGestureButtonData getGestureEncoded(int gesture) {
		return encodedToGesture.get(gesture);
	}

	public CustomPoseGestureButtonData getGestureById(int gesture) {
		return gestureById.get(gesture);
	}

	public Map<String, CustomPose> getCustomPoses() {
		return customPoseByName;
	}

	public int getBlankGesture() {
		return blankGesture;
	}

	public void setBlankGesture(int blankGesture) {
		this.blankGesture = blankGesture;
	}

	public void setPoseResetId(int poseResetId) {
		this.poseResetId = poseResetId;
	}

	public int getPoseResetId() {
		return poseResetId;
	}

	public void addAnimatedTexture(AnimatedTexture tex) {
		animatedTextures.add(tex);
	}

	public void tickAnimated(long time, boolean inGui) {
		for (int i = 0; i < animatedTextures.size(); i++) {
			animatedTextures.get(i).update(time, inGui);
		}
	}

	public List<AbstractGestureButtonData> getNamedActions() {
		return namedActions;
	}

	public Map<String, CommandAction> getCommandActionsMap() {
		return commandActionsMap;
	}

	public Map<String, AbstractGestureButtonData> getNamedActionByKeybind() {
		return namedActionByKeybind;
	}

	public boolean hasPoseAnimations(VanillaPose pose) {
		return !getPoseAnimations(pose).isEmpty();
	}

	public void addCopy(CopyTransform ct) {
		copyTransforms.add(ct);
	}

	public void applyCopy() {
		copyTransforms.forEach(CopyTransform::apply);
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setParams(ParameterDetails params) {
		this.params = params;
	}

	public ParameterDetails getParams() {
		return params;
	}

	public void register(AbstractGestureButtonData btn) {
		btn.onRegistered();
		namedActions.add(btn);
		if (btn.getKeybindId() != null)namedActionByKeybind.put(btn.getKeybindId(), btn);
		btn.getCommandActions().forEach(c -> commandActionsMap.put(c.getName(), c));
		if (btn instanceof CustomPoseGestureButtonData) {
			CustomPoseGestureButtonData b = (CustomPoseGestureButtonData) btn;
			if (b.isPose()) {
				CustomPose pose = new CustomPose(b.getName(), 0);
				if (b.layerCtrl) {
					encodedToPose.put(b.gid, pose);
				}
				poseById.put(b.id, pose);
				customPoseByName.put(b.getName(), pose);
			} else {
				if (b.layerCtrl) {
					encodedToGesture.put(b.gid, b);
				}
				gestureById.put(b.id, b);
			}
		}
	}

	public void register(AnimationTrigger trigger) {
		animations.add(trigger);
		trigger.onPoses.forEach(p -> poseToTriggers.computeIfAbsent(p, __ -> new ArrayList<>()).add(trigger));
	}
}
