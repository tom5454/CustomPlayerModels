package com.tom.cpl.function;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface FloatUnaryOperator extends UnaryOperator<Float> {
	float apply(float x);

	@Override
	default Float apply(final Float x) {
		return apply(x.floatValue());
	}
}
