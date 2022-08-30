package com.tom.cpm.shared.animation;

public interface IManualGesture {
	String getName();
	String getGestureId();
	void play(AnimationRegistry reg);
}
