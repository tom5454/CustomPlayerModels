package com.tom.cpm.shared.animation.interpolator;

import java.util.function.DoubleUnaryOperator;

import com.tom.cpl.math.MathHelper;

public class TrigonometricLoopInterpolator implements Interpolator {
	private float[] values;

	@Override
	public double applyAsDouble(double operand) {
		int frm = Math.abs(MathHelper.floor(operand));
		return MathHelper.trigInt((float) (operand - Math.floor(operand)), values[frm % values.length], values[(frm + 1) % values.length]);
	}

	@Override
	public void init(float[] values, DoubleUnaryOperator setup) {
		setup.applyAsDouble(values[(values.length - 2) % values.length]);
		setup.applyAsDouble(values[(values.length - 1) % values.length]);
		this.values = new float[values.length];
		for (int i = 0; i < values.length; i++) {
			this.values[i] = (float) setup.applyAsDouble(values[i]);
		}
	}
}
