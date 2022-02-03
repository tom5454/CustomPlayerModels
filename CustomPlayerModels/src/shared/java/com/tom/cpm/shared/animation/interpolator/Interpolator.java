package com.tom.cpm.shared.animation.interpolator;

import java.util.function.DoubleUnaryOperator;

import com.tom.cpm.shared.animation.InterpolatorChannel;

public interface Interpolator extends DoubleUnaryOperator {
	void init(float[] values, InterpolatorChannel channel);
}
