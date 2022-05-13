package com.tom.cpl.render;

public class DualBuffer implements VertexBuffer {
	private VertexBuffer first;
	private VertexBuffer second;

	public DualBuffer(VertexBuffer first, VertexBuffer second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public VertexBuffer pos(float x, float y, float z) {
		first.pos(x, y, z);
		second.pos(x, y, z);
		return this;
	}

	@Override
	public VertexBuffer tex(float u, float v) {
		first.tex(u, v);
		second.tex(u, v);
		return this;
	}

	@Override
	public VertexBuffer color(float red, float green, float blue, float alpha) {
		first.color(red, green, blue, alpha);
		second.color(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexBuffer normal(float x, float y, float z) {
		first.normal(x, y, z);
		second.normal(x, y, z);
		return this;
	}

	@Override
	public void endVertex() {
		first.endVertex();
		second.endVertex();
	}

	@Override
	public void finish() {
		first.finish();
		second.finish();
	}
}
