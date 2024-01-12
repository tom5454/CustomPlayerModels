package com.tom.cpm.shared.animation;

import java.util.function.DoubleUnaryOperator;

public interface InterpolationInfo {
	public static final DoubleUnaryOperator NULL_OPERATOR = a -> a;
	public static final InterpolationInfo NULL = () -> NULL_OPERATOR;

	DoubleUnaryOperator createInterpolatorSetup();
}
