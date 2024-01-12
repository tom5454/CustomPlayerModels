package com.tom.cpm.shared.animation;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.function.FloatUnaryOperator;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.definition.ModelDefinition;

public class AnimationNew implements IAnimation {
	private final int duration;
	private final int priority;
	private List<AnimationDriver> handlers = new ArrayList<>();

	public AnimationNew(int priority, int duration) {
		this.duration = duration;
		this.priority = priority;
	}

	public void add(AnimationDriver d) {
		handlers.add(d);
	}

	@Override
	public void animate(long millis, ModelDefinition def, AnimationMode mode) {
		float step = (float) millis % duration / duration;
		for (int i = 0; i < handlers.size(); i++) {
			AnimationDriver p = handlers.get(i);
			p.set(step);
		}
	}

	@Override
	public int getDuration(AnimationMode mode) {
		return duration;
	}

	@Override
	public int getPriority(AnimationMode mode) {
		return priority;
	}

	public static interface AnimationDriver {
		void set(float value);
	}

	public static interface PartAnimationDriver extends AnimationDriver {
		InterpolationInfo getInterpolationInfo();
		void init(ModelDefinition def);

		default AnimationDriver makeDriver(FloatUnaryOperator frameDriver) {
			if (frameDriver == null)return null;
			return t -> set(frameDriver.apply(t));
		}
	}
}
