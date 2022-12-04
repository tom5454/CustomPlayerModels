package com.tom.cpm.shared.animation;

import com.tom.cpm.shared.definition.ModelDefinition;

public interface IAnimation {
	int getDuration();
	int getPriority();
	void animate(long millis, ModelDefinition def);

	default void prepare() {
	}

	default boolean checkAndUpdateRemove() {
		return true;
	}
}
