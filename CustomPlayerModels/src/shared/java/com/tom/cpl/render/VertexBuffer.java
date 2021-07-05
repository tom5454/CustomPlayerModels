package com.tom.cpl.render;

import com.tom.cpl.math.Mat3f;
import com.tom.cpl.math.Mat4f;
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

	void finish();
}
