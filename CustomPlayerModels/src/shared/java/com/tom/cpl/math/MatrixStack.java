package com.tom.cpl.math;

import java.util.Deque;

import com.google.common.collect.Queues;

public class MatrixStack {
	private final Deque<MatrixStack.Entry> stack;

	public MatrixStack() {
		stack = Queues.newArrayDeque();
		Mat4f matrix4f = new Mat4f();
		matrix4f.setIdentity();
		Mat3f matrix3f = new Mat3f();
		matrix3f.setIdentity();
		stack.add(new MatrixStack.Entry(matrix4f, matrix3f));
	}

	public void translate(double x, double y, double z) {
		MatrixStack.Entry matrixstack$entry = this.stack.getLast();
		matrixstack$entry.matrix.mul(Mat4f.makeTranslate((float)x, (float)y, (float)z));
	}

	public void scale(float x, float y, float z) {
		MatrixStack.Entry matrixstack$entry = this.stack.getLast();
		matrixstack$entry.matrix.mul(Mat4f.makeScale(x, y, z));
		if (x == y && y == z) {
			if (x > 0.0F) {
				return;
			}

			matrixstack$entry.normal.mul(-1.0F);
		}

		float f = 1.0F / x;
		float f1 = 1.0F / y;
		float f2 = 1.0F / z;
		float f3 = MathHelper.fastInvCubeRoot(f * f1 * f2);
		matrixstack$entry.normal.mul(Mat3f.makeScaleMatrix(f3 * f, f3 * f1, f3 * f2));
	}

	public void rotate(Quaternion quaternion) {
		MatrixStack.Entry matrixstack$entry = this.stack.getLast();
		matrixstack$entry.matrix.mul(quaternion);
		matrixstack$entry.normal.mul(quaternion);
	}

	public void push() {
		MatrixStack.Entry matrixstack$entry = this.stack.getLast();
		this.stack.addLast(new MatrixStack.Entry(matrixstack$entry.matrix.copy(), matrixstack$entry.normal.copy()));
	}

	public void pop() {
		this.stack.removeLast();
	}

	public MatrixStack.Entry getLast() {
		return this.stack.getLast();
	}

	public boolean clear() {
		return this.stack.size() == 1;
	}

	public static final class Entry {
		private final Mat4f matrix;
		private final Mat3f normal;

		private Entry(Mat4f matrix, Mat3f normal) {
			this.matrix = matrix;
			this.normal = normal;
		}

		public Mat4f getMatrix() {
			return this.matrix;
		}

		public Mat3f getNormal() {
			return this.normal;
		}
	}
}
