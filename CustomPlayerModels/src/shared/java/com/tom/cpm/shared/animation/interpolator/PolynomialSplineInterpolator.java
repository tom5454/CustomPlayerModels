package com.tom.cpm.shared.animation.interpolator;

import java.util.function.DoubleUnaryOperator;

import com.tom.cpl.math.MathHelper;
import com.tom.cpm.externals.org.apache.commons.math3.PolynomialSplineFunction;
import com.tom.cpm.externals.org.apache.commons.math3.SplineInterpolator;
import com.tom.cpm.shared.animation.InterpolatorChannel;

public class PolynomialSplineInterpolator implements Interpolator {
	private static final SplineInterpolator INT = new SplineInterpolator();
	private PolynomialSplineFunction function;

	@Override
	public double applyAsDouble(double operand) {
		return function.value(operand);
	}

	@Override
	public void init(float[] values, InterpolatorChannel channel) {
		int frames = values.length;
		double[] xArr = new double[frames + 2];
		for (int i = 0; i < frames + 2; i++)
			xArr[i] = i - 1;

		double[] yArr = new double[frames + 2];
		DoubleUnaryOperator setup = channel.createInterpolatorSetup();
		for (int j = 0; j < frames + 2; j++)
			yArr[j] = setup.applyAsDouble(values[MathHelper.clamp(j - 1, 0, frames - 1)]);

		function = INT.interpolate(xArr, yArr);
	}
}
