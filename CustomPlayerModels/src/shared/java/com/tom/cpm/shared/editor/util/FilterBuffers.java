package com.tom.cpm.shared.editor.util;

import java.util.function.Predicate;

import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpl.render.VertexBuffer;

public class FilterBuffers {
	private Predicate<NativeRenderType> predicate;

	public FilterBuffers(Predicate<NativeRenderType> predicate) {
		this.predicate = predicate;
	}

	public VBuffers filter(VBuffers in) {
		return in.map(Filtered::new);
	}

	private class Filtered implements VertexBuffer {
		private VertexBuffer b;
		private NativeRenderType nrt;
		private boolean enable;

		public Filtered(NativeRenderType nrt, VertexBuffer b) {
			this.b = b;
			this.nrt = nrt;
		}

		private boolean enable() {
			return enable = predicate.test(nrt);
		}

		@Override
		public VertexBuffer pos(float x, float y, float z) {
			if(enable())b.pos(x, y, z);
			return this;
		}

		@Override
		public VertexBuffer tex(float u, float v) {
			if(enable)b.tex(u, v);
			return this;
		}

		@Override
		public VertexBuffer color(float red, float green, float blue, float alpha) {
			if(enable)b.color(red, green, blue, alpha);
			return this;
		}

		@Override
		public VertexBuffer normal(float x, float y, float z) {
			if(enable)b.normal(x, y, z);
			return this;
		}

		@Override
		public void endVertex() {
			if(enable)b.endVertex();
		}

		@Override
		public void finish() {
			b.finish();
		}
	}
}
