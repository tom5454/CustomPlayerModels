package com.tom.cpl.render;

import com.tom.cpl.math.Mat3f;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack.Entry;

public class TransformedBuffer implements VertexBuffer {
	private VertexBuffer buf;
	private Mat4f matrix;
	private Mat3f normal;

	public TransformedBuffer(VertexBuffer buf, Entry transform) {
		this.buf = buf;
		matrix = transform.getMatrix();
		normal = transform.getNormal();
	}

	@Override
	public VertexBuffer pos(float x, float y, float z) {
		buf.pos(matrix, x, y, z);
		return this;
	}

	@Override
	public VertexBuffer tex(float u, float v) {
		buf.tex(u, v);
		return this;
	}

	@Override
	public VertexBuffer color(float red, float green, float blue, float alpha) {
		buf.color(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexBuffer normal(float x, float y, float z) {
		buf.normal(normal, x, y, z);
		return this;
	}

	@Override
	public void endVertex() {
		buf.endVertex();
	}

	@Override
	public void finish() {
		buf.finish();
	}

}
