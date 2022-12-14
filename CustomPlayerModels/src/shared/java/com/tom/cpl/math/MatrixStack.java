package com.tom.cpl.math;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

import com.google.common.collect.Queues;

public class MatrixStack {
	public static final MatrixStack.Entry NO_RENDER;
	private final Deque<MatrixStack.Entry> stack;

	public MatrixStack() {
		stack = Queues.newArrayDeque();
		Mat4f matrix4f = new Mat4f();
		matrix4f.setIdentity();
		Mat3f matrix3f = new Mat3f();
		matrix3f.setIdentity();
		stack.add(new MatrixStack.Entry(matrix4f, matrix3f));
	}

	private MatrixStack(Deque<Entry> stack) {
		this.stack = stack;
	}

	static {
		MatrixStack s = new MatrixStack();
		s.scale(0, 0, 0);
		NO_RENDER = s.getLast();
	}

	public void translate(double x, double y, double z) {
		MatrixStack.Entry e = this.stack.getLast();
		e.matrix.mul(Mat4f.makeTranslate((float)x, (float)y, (float)z));
	}

	public void scale(float x, float y, float z) {
		MatrixStack.Entry e = this.stack.getLast();
		e.matrix.mul(Mat4f.makeScale(x, y, z));
		if (x == y && y == z) {
			if (x > 0.0F) {
				return;
			}

			e.normal.mul(-1.0F);
		}

		float f = 1.0F / x;
		float f1 = 1.0F / y;
		float f2 = 1.0F / z;
		float f3 = MathHelper.fastInvCubeRoot(f * f1 * f2);
		e.normal.mul(Mat3f.makeScaleMatrix(f3 * f, f3 * f1, f3 * f2));
	}

	public void rotate(Quaternion quaternion) {
		MatrixStack.Entry e = this.stack.getLast();
		e.matrix.mul(quaternion);
		e.normal.mul(quaternion);
	}

	public void push() {
		MatrixStack.Entry e = this.stack.getLast();
		this.stack.addLast(new MatrixStack.Entry(e.matrix.copy(), e.normal.copy()));
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

	public MatrixStack fork() {
		return new MatrixStack(stack.stream().map(Entry::copy).collect(Collectors.toCollection(ArrayDeque::new)));
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

		public float[] getMatrixArray() {
			return matrix.toArray();
		}

		public float[] getNormalArray() {
			return new Mat4f(normal).toArray();
		}

		public float[] getNormalArray3() {
			return normal.toArray();
		}

		public Entry copy() {
			return new MatrixStack.Entry(matrix.copy(), normal.copy());
		}
	}

	public Entry storeLast() {
		MatrixStack.Entry e = this.stack.getLast();
		return e.copy();
	}

	public void setLast(Entry in) {
		pop();
		this.stack.addLast(in.copy());
	}

	public void mul(Entry matrix) {
		MatrixStack.Entry e = this.stack.getLast();
		e.matrix.mul(matrix.matrix);
		e.normal.mul(e.normal);
	}
}
