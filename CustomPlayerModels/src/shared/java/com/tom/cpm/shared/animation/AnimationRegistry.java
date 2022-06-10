package com.tom.cpm.shared.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpm.shared.model.CopyTransform;

public class AnimationRegistry {
	private List<AnimatedTexture> animatedTextures = new ArrayList<>();
	private Map<IPose, List<IAnimation>> animations = new HashMap<>();
	private Map<Integer, IPose> encodedToPose = new HashMap<>();
	private Map<Integer, Gesture> encodedToGesture = new HashMap<>();
	private Map<IPose, Integer> poseToEncoded = new HashMap<>();
	private Map<Gesture, Integer> gestureToEncoded = new HashMap<>();
	private Map<String, Gesture> gestures = new HashMap<>();
	private Map<String, CustomPose> customPoses = new HashMap<>();
	private List<CopyTransform> copyTransforms = new ArrayList<>();
	private int blankGesture;
	private int poseResetId;

	public List<IAnimation> getPoseAnimations(IPose id) {
		return animations.getOrDefault(id, Collections.emptyList());
	}

	public IPose getPose(int gesture, IPose pose) {
		return encodedToPose.getOrDefault(gesture, pose);
	}

	public Gesture getGesture(int gesture) {
		return encodedToGesture.get(gesture);
	}

	public static class Gesture {
		public List<IAnimation> animation;
		public boolean isLoop;
		public String name;

		public Gesture(List<IAnimation> animation, String name, boolean isLoop) {
			this.animation = animation;
			this.name = name;
			this.isLoop = isLoop;
		}
	}

	public void register(IPose pose, IAnimation anim) {
		animations.computeIfAbsent(pose, k -> new ArrayList<>()).add(anim);
	}

	public void register(int gid, IPose pose) {
		encodedToPose.put(gid, pose);
		poseToEncoded.put(pose, gid);
	}

	public void register(CustomPose pose) {
		customPoses.put(pose.getName(), pose);
	}

	public void register(Gesture gesture) {
		gestures.put(gesture.name, gesture);
	}

	public void register(int gid, Gesture gesture) {
		encodedToGesture.put(gid, gesture);
		gestureToEncoded.put(gesture, gid);
	}

	public int getEncoded(Gesture g) {
		return gestureToEncoded.getOrDefault(g, -1);
	}

	public int getEncoded(CustomPose pose) {
		return poseToEncoded.getOrDefault(pose, -1);
	}

	public Map<String, Gesture> getGestures() {
		return gestures;
	}

	public Map<String, CustomPose> getCustomPoses() {
		return customPoses;
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

	public void tickAnimated(long time) {
		for (int i = 0; i < animatedTextures.size(); i++) {
			animatedTextures.get(i).update(time);
		}
	}

	public Map<IPose, List<IAnimation>> getAnimations() {
		return animations;
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
}
