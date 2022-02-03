package com.tom.cpm.shared.model.render;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.MatrixStack.Entry;

public class ItemTransform {
	private Entry matrix;

	public Entry getMatrix() {
		return matrix == null ? MatrixStack.NO_RENDER : matrix;
	}

	public void set(MatrixStack stack, boolean doRender) {
		if(doRender)
			matrix = stack.storeLast();
		else
			matrix = null;
	}
}
