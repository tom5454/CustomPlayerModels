package com.tom.cpm.shared.animation;

import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.definition.ModelDefinition;

public interface IAnimation {
	int getDuration(AnimationMode mode);
	int getPriority(AnimationMode mode);
	void animate(long millis, ModelDefinition def, AnimationMode mode);

	default void prepare(AnimationMode mode) {
	}

	default boolean checkAndUpdateRemove(AnimationMode mode) {
		return true;
	}

	default boolean useTriggerTime() {
		return true;
	}
}
