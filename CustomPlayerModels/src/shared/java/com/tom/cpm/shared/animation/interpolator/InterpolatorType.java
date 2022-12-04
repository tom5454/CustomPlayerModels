package com.tom.cpm.shared.animation.interpolator;

import java.util.function.Supplier;

public enum InterpolatorType {
	POLY_LOOP(PolynomialSplineLoopInterpolator::new),
	POLY_SINGLE(PolynomialSplineInterpolator::new, POLY_LOOP),
	LINEAR_LOOP(LinearLoopInterpolator::new),
	LINEAR_SINGLE(LinearInterpolator::new, LINEAR_LOOP),
	NO_INTERPOLATE(NoInterpolate::new),
	TRIG_LOOP(TrigonometricLoopInterpolator::new),
	TRIG_SINGLE(TrigonometricInterpolator::new, TRIG_LOOP),
	;
	public static final InterpolatorType[] VALUES = values();
	private final Supplier<Interpolator> factory;
	private InterpolatorType altLoop, altSingle;

	private InterpolatorType(Supplier<Interpolator> factory) {
		this.factory = factory;
	}

	private InterpolatorType(Supplier<Interpolator> factory, InterpolatorType loop) {
		this.factory = factory;
		altLoop = loop;
		loop.altSingle = this;
	}

	public Interpolator create() {
		return factory.get();
	}

	public InterpolatorType getAlt(boolean loop) {
		if(loop) {
			if(altLoop != null)return altLoop;
		} else {
			if(altSingle != null)return altSingle;
		}
		return this;
	}
}
