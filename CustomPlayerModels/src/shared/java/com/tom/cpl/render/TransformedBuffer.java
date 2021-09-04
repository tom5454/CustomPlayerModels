package com.tom.cpl.render;

import com.tom.cpl.math.Mat3f;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack;

public class TransformedBuffer extends WrappedBuffer {
	private final Mat4f model;
	private final Mat3f normal;

	public TransformedBuffer(VertexBuffer buf, MatrixStack stack) {
		super(buf);
		model = stack.getLast().getMatrix();
		normal = stack.getLast().getNormal();
	}

	@Override
	public VertexBuffer pos(float x, float y, float z) {
		buffer.pos(model, x, y, z);
		return this;
	}

	@Override
	public VertexBuffer normal(float x, float y, float z) {
		buffer.normal(normal, x, y, z);
		return this;
	}
}
