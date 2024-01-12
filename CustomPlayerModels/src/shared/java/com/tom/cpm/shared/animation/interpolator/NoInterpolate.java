package com.tom.cpm.shared.animation.interpolator;

import java.util.function.DoubleUnaryOperator;

public class NoInterpolate implements Interpolator {
	private float[] values;

	@Override
	public double applyAsDouble(double operand) {
		return values[Math.abs((int) operand) % values.length];
	}

	@Override
	public void init(float[] values, DoubleUnaryOperator setup) {
		this.values = values;
	}

}
