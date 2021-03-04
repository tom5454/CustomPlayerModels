package com.tom.cpm.shared.animation;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.anim.AnimFrame;

public interface Interpolator extends DoubleUnaryOperator {
	void init(List<AnimFrame> frames, ModelElement component, InterpolatorChannel channel);
	void init(float[] values, InterpolatorChannel channel);
}
