package com.tom.cpl.render;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.math.MatrixStack;

public class ListBuffer implements VertexBuffer {
	private List<Entry> toReplay;
	private Entry entry;

	public ListBuffer() {
		toReplay = new ArrayList<>();
		entry = new Entry();
	}

	@Override
	public VertexBuffer pos(float x, float y, float z) {
		entry.x = x;
		entry.y = y;
		entry.z = z;
		return this;
	}

	@Override
	public VertexBuffer color(float red, float green, float blue, float alpha) {
		entry.red = red;
		entry.green = green;
		entry.blue = blue;
		entry.alpha = alpha;
		return this;
	}

	@Override
	public VertexBuffer tex(float u, float v) {
		entry.u = u;
		entry.v = v;
		return this;
	}

	@Override
	public VertexBuffer normal(float x, float y, float z) {
		entry.nx = x;
		entry.ny = y;
		entry.nz = z;
		return this;
	}

	@Override
	public void endVertex() {
		toReplay.add(entry);
		entry = new Entry();
	}

	@Override
	public void finish() {
	}

	private static class Entry {
		private float x, y, z;
		private float red, green, blue, alpha;
		private float u, v;
		private float nx, ny, nz;

		public void replay(MatrixStack stack, VertexBuffer parent) {
			parent.addVertex(stack.getLast(), x, y, z, red, green, blue, alpha, u, v, nx, ny, nz);
		}

		public void replay(MatrixStack stack, VertexBuffer parent, float red, float green, float blue, float alpha) {
			parent.addVertex(stack.getLast(), x, y, z, red, green, blue, alpha, u, v, nx, ny, nz);
		}

		public void replay(VertexBuffer parent) {
			parent.addVertex(x, y, z, red, green, blue, alpha, u, v, nx, ny, nz);
		}

		public void replay(VertexBuffer parent, float red, float green, float blue, float alpha) {
			parent.addVertex(x, y, z, red, green, blue, alpha, u, v, nx, ny, nz);
		}
	}

	public void draw(MatrixStack stack, VertexBuffer buf) {
		if(!toReplay.isEmpty()) {
			for (Entry entry : toReplay) {
				entry.replay(stack, buf);
			}
		}
	}

	public void draw(MatrixStack stack, VertexBuffer buf, float red, float green, float blue, float alpha) {
		if(!toReplay.isEmpty()) {
			for (Entry entry : toReplay) {
				entry.replay(stack, buf, red, green, blue, alpha);
			}
		}
	}

	public void draw(VertexBuffer buf) {
		if(!toReplay.isEmpty()) {
			for (Entry entry : toReplay) {
				entry.replay(buf);
			}
		}
	}

	public void draw(VertexBuffer buf, float red, float green, float blue, float alpha) {
		if(!toReplay.isEmpty()) {
			for (Entry entry : toReplay) {
				entry.replay( buf, red, green, blue, alpha);
			}
		}
	}
}
