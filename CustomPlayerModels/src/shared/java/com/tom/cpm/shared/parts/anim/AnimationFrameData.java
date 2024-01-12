package com.tom.cpm.shared.parts.anim;

import com.tom.cpl.function.FloatUnaryOperator;
import com.tom.cpm.shared.animation.InterpolationInfo;

public interface AnimationFrameData {
	AnimationFrameDataType getType();
	FloatUnaryOperator createDriver(InterpolationInfo part);
}