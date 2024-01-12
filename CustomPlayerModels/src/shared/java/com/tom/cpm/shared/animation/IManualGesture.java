package com.tom.cpm.shared.animation;

import com.tom.cpm.shared.config.Player;

public interface IManualGesture {
	String getName();
	String getGestureId();
	void play(AnimationRegistry reg, Player<?> player);
	AnimationType getType();
	int getOrder();
	boolean isCommand();
	boolean isLayerControlled();

	default boolean isProperty() {
		return false;
	}

	default float getDefaultValue() {
		return 0;
	}
}
