package com.tom.cpm.shared.animation;

import com.tom.cpm.externals.org.apache.commons.math3.PolynomialSplineFunction;
import com.tom.cpm.externals.org.apache.commons.math3.SplineInterpolator;
import com.tom.cpm.shared.definition.ModelDefinition;

public class Animation {
	private final IModelComponent[] componentIDs;
	private final PolynomialSplineFunction[][] psfs;
	private final boolean[][] show;
	private boolean add;

	protected final int duration;
	private final int frames;

	/**
	 *
	 * @param components2
	 * @param data
	 *            [component][attributeId][frame]
	 *
	 *            attributeIDs: 0: rotationPointX 1: rotationPointY 2:
	 *            rotationPointZ 3: rotationX 4: rotationY 5: rotationZ
	 */
	public Animation(IModelComponent[] components2, float[][][] data, boolean[][] show, int duration, boolean add) {
		this.componentIDs = components2;
		this.duration = duration;
		this.show = show;
		this.add = add;

		if(components2.length == 0) {
			frames = 0;
			psfs = new PolynomialSplineFunction[0][];
			return;
		}

		int components = data.length;
		frames = data[0][0].length;

		psfs = new PolynomialSplineFunction[components2.length][9];

		SplineInterpolator si = new SplineInterpolator();

		double[] xArr = new double[frames + 5];
		for (int i = 0; i < frames + 5; i++)
			xArr[i] = i - 2;

		for (int component = 0; component < components; component++) {
			double[][] yArr = new double[9][frames + 5];
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < frames + 5; j++)
					yArr[i][j] = data[component][i][(j + frames - 2) % frames];

				psfs[component][i] = si.interpolate(xArr, yArr[i]);
			}
		}
	}

	protected void animate(long millis, ModelDefinition def) {
		if(frames == 0)return;
		float step = (float) millis % duration / duration * frames;
		for (int componentId = 0; componentId < componentIDs.length; componentId++) {
			IModelComponent component = componentIDs[componentId];
			component.setRotation(add, getValue(componentId, 3, step), getValue(componentId, 4, step), getValue(componentId, 5, step));
			component.setPosition(add, getValue(componentId, 0, step), getValue(componentId, 1, step), getValue(componentId, 2, step));
			component.setColor(getValue(componentId, 6, step), getValue(componentId, 7, step), getValue(componentId, 8, step));
			component.setVisible(show[componentId][(int) step]);
		}
	}

	private float getValue(int component, int attribute, double time) {
		return (float) psfs[component][attribute].value(time);
	}
}
