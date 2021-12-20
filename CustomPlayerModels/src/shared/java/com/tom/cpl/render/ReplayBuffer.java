package com.tom.cpl.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ReplayBuffer implements VertexBuffer {
	private Supplier<VertexBuffer> parent;
	private List<Entry> toReplay;
	private Entry entry;

	public ReplayBuffer(Supplier<VertexBuffer> parent) {
		this.parent = parent;
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
		if(!toReplay.isEmpty()) {
			VertexBuffer p = parent.get();
			for (Entry entry : toReplay) {
				entry.replay(p);
			}
			p.finish();
		}
	}

	private static class Entry {
		private float x, y, z;
		private float red, green, blue, alpha;
		private float u, v;
		private float nx, ny, nz;

		public void replay(VertexBuffer parent) {
			parent.addVertex(x, y, z, red, green, blue, alpha, u, v, nx, ny, nz);
		}
	}
}
