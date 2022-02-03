package com.tom.cpm.shared.model.builtin;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.render.VertexBuffer;

public abstract class SimpleModel {
	public int textureWidth = 64;
	public int textureHeight = 32;

	public abstract void render(MatrixStack stack, VertexBuffer buf);
	public abstract String getTexture();
}
