package com.tom.cpm.shared.animation;

public interface IManualGesture {
	String getName();
	String getGestureId();
	void play(AnimationRegistry reg);
	AnimationType getType();
	int getOrder();
	boolean isCommand();
	boolean isLayerControlled();

	default boolean isProperty() {
		return false;
	}
}
