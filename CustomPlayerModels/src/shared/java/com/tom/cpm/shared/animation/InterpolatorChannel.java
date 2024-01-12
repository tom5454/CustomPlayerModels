package com.tom.cpm.shared.animation;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import com.tom.cpm.shared.util.RotationInterpolator;

public enum InterpolatorChannel implements InterpolationInfo {
	POS_X(0),
	POS_Y(1),
	POS_Z(2),
	ROT_X(RotationInterpolator::createRad, 3),
	ROT_Y(RotationInterpolator::createRad, 4),
	ROT_Z(RotationInterpolator::createRad, 5),
	COLOR_R(6),
	COLOR_G(7),
	COLOR_B(8),
	SCALE_X(9, 1),
	SCALE_Y(10, 1),
	SCALE_Z(11, 1),
	;
	public static final InterpolatorChannel[] VALUES = values();
	private final Supplier<DoubleUnaryOperator> interpolatorSetupFactory;
	private final int id;
	public final int defaultValue;

	private InterpolatorChannel(int id) {
		this(id, 0);
	}

	private InterpolatorChannel(int id, int defVal) {
		this(() -> a -> a, id, defVal);
	}

	private InterpolatorChannel(Supplier<DoubleUnaryOperator> interpolatorSetupFactory, int id) {
		this(interpolatorSetupFactory, id, 0);
	}

	private InterpolatorChannel(Supplier<DoubleUnaryOperator> interpolatorSetupFactory, int id, int defVal) {
		this.interpolatorSetupFactory = interpolatorSetupFactory;
		this.id = id;
		this.defaultValue = defVal;
	}

	@Override
	public DoubleUnaryOperator createInterpolatorSetup() {
		return interpolatorSetupFactory.get();
	}

	public int channelID() {
		return id;
	}
}
