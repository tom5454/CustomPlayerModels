package com.tom.cpm.shared.animation;

import java.util.function.DoubleUnaryOperator;

public interface Interpolator extends DoubleUnaryOperator {
	void init(float[] values, InterpolatorChannel channel);
}
