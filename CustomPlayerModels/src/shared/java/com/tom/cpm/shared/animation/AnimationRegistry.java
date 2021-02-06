package com.tom.cpm.shared.animation;

import java.util.HashMap;
import java.util.Map;

public class AnimationRegistry {
	public static final Animation NULL = new Animation(new IModelComponent[0], new float[0][][], new boolean[0][], 1, true);
	private Map<IPose, Animation> animations = new HashMap<>();
	private Map<Integer, IPose> encodedToPose = new HashMap<>();
	private Map<Integer, Gesture> encodedToGesture = new HashMap<>();
	private Map<IPose, Integer> poseToEncoded = new HashMap<>();
	private Map<Gesture, Integer> gestureToEncoded = new HashMap<>();
	private Map<String, Gesture> gestures = new HashMap<>();
	private Map<String, CustomPose> customPoses = new HashMap<>();
	private int blankGesture;
	private int poseResetId;

	public Animation getPoseAnimation(IPose id) {
		return animations.getOrDefault(id, NULL);
	}

	public IPose getPose(int gesture, IPose pose) {
		return encodedToPose.getOrDefault(gesture, pose);
	}

	public Gesture getGesture(int gesture) {
		return encodedToGesture.get(gesture);
	}

	public static class Gesture {
		public Animation animation;
		public boolean isLoop;
		public String name;

		public Gesture(Animation animation, String name, boolean isLoop) {
			this.animation = animation;
			this.name = name;
			this.isLoop = isLoop;
		}
	}

	public void register(IPose pose, Animation anim) {
		animations.put(pose, anim);
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
}
