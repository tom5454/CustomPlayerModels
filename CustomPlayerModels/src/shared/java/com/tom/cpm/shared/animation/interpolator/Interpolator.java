package com.tom.cpm.shared.animation.interpolator;

import java.util.function.DoubleUnaryOperator;

public interface Interpolator extends DoubleUnaryOperator {
	void init(float[] values, DoubleUnaryOperator setup);
}
