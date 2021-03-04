package com.tom.cpm.shared.animation;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import com.tom.cpm.externals.org.apache.commons.math3.PolynomialSplineFunction;
import com.tom.cpm.externals.org.apache.commons.math3.SplineInterpolator;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.IElem;

public class PolynomialSplineInterpolator implements Interpolator {
	private static final SplineInterpolator INT = new SplineInterpolator();
	private PolynomialSplineFunction function;

	@Override
	public double applyAsDouble(double operand) {
		return function.value(operand);
	}

	@Override
	public void init(List<AnimFrame> frames, ModelElement component, InterpolatorChannel channel) {
		double[] xArr = new double[frames.size() + 5];
		for (int i = 0; i < frames.size() + 5; i++)
			xArr[i] = i - 2;

		double[] yArr = new double[frames.size() + 5];
		DoubleUnaryOperator setup = channel.createInterpolatorSetup();
		{
			IElem data = frames.get(0).getData(component);
			if (data == null)
				data = component;
			setup.applyAsDouble(data.part(channel));
			data = frames.get(frames.size() - 1).getData(component);
			if (data == null)
				data = component;
			setup.applyAsDouble(data.part(channel));
		}
		for (int j = 0; j < frames.size() + 5; j++) {
			IElem data = frames.get((j + frames.size() - 2) % frames.size()).getData(component);
			if (data == null)
				data = component;
			yArr[j] = setup.applyAsDouble(data.part(channel));
		}

		function = INT.interpolate(xArr, yArr);
	}

	@Override
	public void init(float[] values, InterpolatorChannel channel) {
		int frames = values.length;
		double[] xArr = new double[frames + 5];
		for (int i = 0; i < frames + 5; i++)
			xArr[i] = i - 2;

		double[] yArr = new double[frames + 5];
		DoubleUnaryOperator setup = channel.createInterpolatorSetup();
		setup.applyAsDouble(values[0]);
		setup.applyAsDouble(values[values.length - 1]);
		for (int j = 0; j < frames + 5; j++)
			yArr[j] = setup.applyAsDouble(values[(j + frames - 2) % frames]);

		function = INT.interpolate(xArr, yArr);
	}
}
