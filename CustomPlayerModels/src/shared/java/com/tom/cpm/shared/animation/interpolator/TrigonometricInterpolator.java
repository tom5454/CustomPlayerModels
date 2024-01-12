package com.tom.cpm.shared.animation.interpolator;

import java.util.function.DoubleUnaryOperator;

import com.tom.cpl.math.MathHelper;

public class TrigonometricInterpolator implements Interpolator {
	private float[] values;

	@Override
	public double applyAsDouble(double operand) {
		double v = operand / values.length * (values.length - 1);
		int frm = Math.abs(MathHelper.floor(v));
		return MathHelper.trigInt((float) (v - Math.floor(v)), values[Math.min(frm, values.length - 1)], values[Math.min(frm + 1, values.length - 1)]);
	}

	@Override
	public void init(float[] values, DoubleUnaryOperator setup) {
		this.values = new float[values.length];
		for (int i = 0; i < values.length; i++) {
			this.values[i] = (float) setup.applyAsDouble(values[i]);
		}
	}
}
