package com.tom.cpm.shared.model.render;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.render.VertexBuffer;

public interface Mesh {
	public void draw(MatrixStack matrixStackIn, VertexBuffer bufferIn, float red, float green, float blue, float alpha);
	public RenderMode getLayer();
	public void free();
}