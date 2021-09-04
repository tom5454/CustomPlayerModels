package com.tom.cpl.render;

public class WrappedBuffer implements VertexBuffer {
	protected final VertexBuffer buffer;

	public WrappedBuffer(VertexBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public VertexBuffer pos(float x, float y, float z) {
		return buffer.pos(x, y, z);
	}

	@Override
	public VertexBuffer tex(float u, float v) {
		return buffer.tex(u, v);
	}

	@Override
	public VertexBuffer color(float red, float green, float blue, float alpha) {
		return buffer.color(red, green, blue, alpha);
	}

	@Override
	public VertexBuffer normal(float x, float y, float z) {
		return buffer.normal(x, y, z);
	}

	@Override
	public void endVertex() {
		buffer.endVertex();
	}

	@Override
	public void finish() {
		buffer.finish();
	}
}
