package com.tom.cpm.shared.animation.interpolator;

import java.util.function.Supplier;

public enum InterpolatorType {
	POLY_LOOP(PolynomialSplineLoopInterpolator::new),
	POLY_SINGLE(PolynomialSplineInterpolator::new),
	LINEAR_LOOP(LinearLoopInterpolator::new),
	LINEAR_SINGLE(LinearInterpolator::new),
	NO_INTERPOLATE(NoInterpolate::new),
	;
	public static final InterpolatorType[] VALUES = values();
	private final Supplier<Interpolator> factory;

	private InterpolatorType(Supplier<Interpolator> factory) {
		this.factory = factory;
	}

	public Interpolator create() {
		return factory.get();
	}

	public InterpolatorType getAlt(boolean loop) {
		if(loop) {
			if(this == POLY_SINGLE)return POLY_LOOP;
			if(this == LINEAR_SINGLE)return LINEAR_LOOP;
		} else {
			if(this == POLY_LOOP)return POLY_SINGLE;
			if(this == LINEAR_LOOP)return LINEAR_SINGLE;
		}
		return this;
	}
}
