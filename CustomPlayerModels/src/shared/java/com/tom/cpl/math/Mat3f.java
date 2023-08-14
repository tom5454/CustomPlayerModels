package com.tom.cpl.math;

public class Mat3f {
	protected float m00;
	protected float m01;
	protected float m02;
	protected float m10;
	protected float m11;
	protected float m12;
	protected float m20;
	protected float m21;
	protected float m22;

	public Mat3f() {
	}

	public Mat3f(Quaternion quaternionIn) {
		float f = quaternionIn.getX();
		float f1 = quaternionIn.getY();
		float f2 = quaternionIn.getZ();
		float f3 = quaternionIn.getW();
		float f4 = 2.0F * f * f;
		float f5 = 2.0F * f1 * f1;
		float f6 = 2.0F * f2 * f2;
		this.m00 = 1.0F - f5 - f6;
		this.m11 = 1.0F - f6 - f4;
		this.m22 = 1.0F - f4 - f5;
		float f7 = f * f1;
		float f8 = f1 * f2;
		float f9 = f2 * f;
		float f10 = f * f3;
		float f11 = f1 * f3;
		float f12 = f2 * f3;
		this.m10 = 2.0F * (f7 + f12);
		this.m01 = 2.0F * (f7 - f12);
		this.m20 = 2.0F * (f9 - f11);
		this.m02 = 2.0F * (f9 + f11);
		this.m21 = 2.0F * (f8 + f10);
		this.m12 = 2.0F * (f8 - f10);
	}

	public static Mat3f makeScaleMatrix(float p_226117_0_, float p_226117_1_, float p_226117_2_) {
		Mat3f matrix3f = new Mat3f();
		matrix3f.m00 = p_226117_0_;
		matrix3f.m11 = p_226117_1_;
		matrix3f.m22 = p_226117_2_;
		return matrix3f;
	}

	public Mat3f(Mat4f matrixIn) {
		this.m00 = matrixIn.m00;
		this.m01 = matrixIn.m01;
		this.m02 = matrixIn.m02;
		this.m10 = matrixIn.m10;
		this.m11 = matrixIn.m11;
		this.m12 = matrixIn.m12;
		this.m20 = matrixIn.m20;
		this.m21 = matrixIn.m21;
		this.m22 = matrixIn.m22;
	}

	public Mat3f(Mat3f matrixIn) {
		this.m00 = matrixIn.m00;
		this.m01 = matrixIn.m01;
		this.m02 = matrixIn.m02;
		this.m10 = matrixIn.m10;
		this.m11 = matrixIn.m11;
		this.m12 = matrixIn.m12;
		this.m20 = matrixIn.m20;
		this.m21 = matrixIn.m21;
		this.m22 = matrixIn.m22;
	}

	public Mat3f(float m00, float m01, float m02,
			float m10, float m11, float m12,
			float m20, float m21, float m22) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
	}

	@Override
	public boolean equals(Object p_equals_1_) {
		if (this == p_equals_1_) {
			return true;
		} else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
			Mat3f matrix3f = (Mat3f)p_equals_1_;
			return Float.compare(matrix3f.m00, this.m00) == 0 && Float.compare(matrix3f.m01, this.m01) == 0 && Float.compare(matrix3f.m02, this.m02) == 0 && Float.compare(matrix3f.m10, this.m10) == 0 && Float.compare(matrix3f.m11, this.m11) == 0 && Float.compare(matrix3f.m12, this.m12) == 0 && Float.compare(matrix3f.m20, this.m20) == 0 && Float.compare(matrix3f.m21, this.m21) == 0 && Float.compare(matrix3f.m22, this.m22) == 0;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int i = this.m00 != 0.0F ? Float.floatToIntBits(this.m00) : 0;
		i = 31 * i + (this.m01 != 0.0F ? Float.floatToIntBits(this.m01) : 0);
		i = 31 * i + (this.m02 != 0.0F ? Float.floatToIntBits(this.m02) : 0);
		i = 31 * i + (this.m10 != 0.0F ? Float.floatToIntBits(this.m10) : 0);
		i = 31 * i + (this.m11 != 0.0F ? Float.floatToIntBits(this.m11) : 0);
		i = 31 * i + (this.m12 != 0.0F ? Float.floatToIntBits(this.m12) : 0);
		i = 31 * i + (this.m20 != 0.0F ? Float.floatToIntBits(this.m20) : 0);
		i = 31 * i + (this.m21 != 0.0F ? Float.floatToIntBits(this.m21) : 0);
		return 31 * i + (this.m22 != 0.0F ? Float.floatToIntBits(this.m22) : 0);
	}

	@Override
	public String toString() {
		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.append("Matrix3f:\n");
		stringbuilder.append(this.m00);
		stringbuilder.append(" ");
		stringbuilder.append(this.m01);
		stringbuilder.append(" ");
		stringbuilder.append(this.m02);
		stringbuilder.append("\n");
		stringbuilder.append(this.m10);
		stringbuilder.append(" ");
		stringbuilder.append(this.m11);
		stringbuilder.append(" ");
		stringbuilder.append(this.m12);
		stringbuilder.append("\n");
		stringbuilder.append(this.m20);
		stringbuilder.append(" ");
		stringbuilder.append(this.m21);
		stringbuilder.append(" ");
		stringbuilder.append(this.m22);
		stringbuilder.append("\n");
		return stringbuilder.toString();
	}

	public void setIdentity() {
		this.m00 = 1.0F;
		this.m01 = 0.0F;
		this.m02 = 0.0F;
		this.m10 = 0.0F;
		this.m11 = 1.0F;
		this.m12 = 0.0F;
		this.m20 = 0.0F;
		this.m21 = 0.0F;
		this.m22 = 1.0F;
	}

	public void mul(Mat3f p_226118_1_) {
		float f = this.m00 * p_226118_1_.m00 + this.m01 * p_226118_1_.m10 + this.m02 * p_226118_1_.m20;
		float f1 = this.m00 * p_226118_1_.m01 + this.m01 * p_226118_1_.m11 + this.m02 * p_226118_1_.m21;
		float f2 = this.m00 * p_226118_1_.m02 + this.m01 * p_226118_1_.m12 + this.m02 * p_226118_1_.m22;
		float f3 = this.m10 * p_226118_1_.m00 + this.m11 * p_226118_1_.m10 + this.m12 * p_226118_1_.m20;
		float f4 = this.m10 * p_226118_1_.m01 + this.m11 * p_226118_1_.m11 + this.m12 * p_226118_1_.m21;
		float f5 = this.m10 * p_226118_1_.m02 + this.m11 * p_226118_1_.m12 + this.m12 * p_226118_1_.m22;
		float f6 = this.m20 * p_226118_1_.m00 + this.m21 * p_226118_1_.m10 + this.m22 * p_226118_1_.m20;
		float f7 = this.m20 * p_226118_1_.m01 + this.m21 * p_226118_1_.m11 + this.m22 * p_226118_1_.m21;
		float f8 = this.m20 * p_226118_1_.m02 + this.m21 * p_226118_1_.m12 + this.m22 * p_226118_1_.m22;
		this.m00 = f;
		this.m01 = f1;
		this.m02 = f2;
		this.m10 = f3;
		this.m11 = f4;
		this.m12 = f5;
		this.m20 = f6;
		this.m21 = f7;
		this.m22 = f8;
	}


	public void mul(Quaternion p_226115_1_) {
		this.mul(new Mat3f(p_226115_1_));
	}

	public void mul(float scale) {
		this.m00 *= scale;
		this.m01 *= scale;
		this.m02 *= scale;
		this.m10 *= scale;
		this.m11 *= scale;
		this.m12 *= scale;
		this.m20 *= scale;
		this.m21 *= scale;
		this.m22 *= scale;
	}

	public Mat3f copy() {
		return new Mat3f(this);
	}

	public float[] toArray() {
		float[] values = new float[9];
		values[0] = m00;
		values[1] = m01;
		values[2] = m02;
		values[3] = m10;
		values[4] = m11;
		values[5] = m12;
		values[6] = m20;
		values[7] = m21;
		values[8] = m22;
		return values;
	}
}
