package com.tom.cpm.shared.animation;

public enum AnimationType {
	POSE,
	CUSTOM_POSE,
	GESTURE,
	LAYER,
	VALUE_LAYER,
	SETUP,
	FINISH,
	;

	public static final AnimationType[] VALUES = values();

	public boolean isCustom() {
		return this != POSE;
	}

	public boolean isLayer() {
		return this == AnimationType.LAYER || this == AnimationType.VALUE_LAYER;
	}

	public boolean canLoop() {
		return this == GESTURE;
	}

	public boolean isStaged() {
		return this == SETUP || this == FINISH;
	}
}
