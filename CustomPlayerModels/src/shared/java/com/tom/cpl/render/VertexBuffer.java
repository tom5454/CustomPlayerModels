package com.tom.cpl.render;

import com.tom.cpl.math.Mat3f;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;

public interface VertexBuffer {
	VertexBuffer pos(float x, float y, float z);
	VertexBuffer tex(float u, float v);
	VertexBuffer color(float red, float green, float blue, float alpha);
	VertexBuffer normal(float x, float y, float z);
	void endVertex();

	default void addVertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, float normalX, float normalY, float normalZ) {
		this.pos(x, y, z);
		this.color(red, green, blue, alpha);
		this.tex(texU, texV);
		this.normal(normalX, normalY, normalZ);
		this.endVertex();
	}

	default void addVertex(MatrixStack.Entry mat, float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, float normalX, float normalY, float normalZ) {
		this.pos(mat.getMatrix(), x, y, z);
		this.color(red, green, blue, alpha);
		this.tex(texU, texV);
		this.normal(mat.getNormal(), normalX, normalY, normalZ);
		this.endVertex();
	}

	default VertexBuffer pos(Mat4f matrixIn, float x, float y, float z) {
		Vec4f vector4f = new Vec4f(x, y, z, 1.0F);
		vector4f.transform(matrixIn);
		return this.pos(vector4f.x, vector4f.y, vector4f.z);
	}

	default VertexBuffer normal(Mat3f matrix, float x, float y, float z) {
		Vec3f vec3f = new Vec3f(x, y, z);
		vec3f.transform(matrix);
		return this.normal(vec3f.x, vec3f.y, vec3f.z);
	}

	default VertexBuffer color(int argb) {
		int a = ((argb & 0xff000000) >>> 24);
		int r = ((argb & 0x00ff0000) >> 16);
		int g = ((argb & 0x0000ff00) >> 8);
		int b =  argb & 0x000000ff;
		color(r / 255f, g / 255f, b / 255f, a / 255f);
		return this;
	}

	void finish();

	public static final VertexBuffer NULL = new VertexBuffer() {

		@Override
		public VertexBuffer pos(float x, float y, float z) {
			return this;
		}

		@Override
		public VertexBuffer tex(float u, float v) {
			return this;
		}

		@Override
		public VertexBuffer color(float red, float green, float blue, float alpha) {
			return this;
		}

		@Override
		public VertexBuffer normal(float x, float y, float z) {
			return this;
		}

		@Override
		public void endVertex() {}

		@Override
		public void finish() {}
	};
}
