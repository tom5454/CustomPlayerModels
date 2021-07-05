package com.tom.cpl.math;

public class Quaternion {
	public static final Quaternion ONE = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
	private float x;
	private float y;
	private float z;
	private float w;

	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Quaternion(Vec3f axis, float angle, boolean degrees) {
		if (degrees) {
			angle *= ((float)Math.PI / 180F);
		}

		float f = sin(angle / 2.0F);
		this.x = axis.x * f;
		this.y = axis.y * f;
		this.z = axis.z * f;
		this.w = cos(angle / 2.0F);
	}

	@Override
	public boolean equals(Object p_equals_1_) {
		if (this == p_equals_1_) {
			return true;
		} else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
			Quaternion quaternion = (Quaternion)p_equals_1_;
			if (Float.compare(quaternion.x, this.x) != 0) {
				return false;
			} else if (Float.compare(quaternion.y, this.y) != 0) {
				return false;
			} else if (Float.compare(quaternion.z, this.z) != 0) {
				return false;
			} else {
				return Float.compare(quaternion.w, this.w) == 0;
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int i = Float.floatToIntBits(this.x);
		i = 31 * i + Float.floatToIntBits(this.y);
		i = 31 * i + Float.floatToIntBits(this.z);
		return 31 * i + Float.floatToIntBits(this.w);
	}

	@Override
	public String toString() {
		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.append("Quaternion[").append(this.getW()).append(" + ");
		stringbuilder.append(this.getX()).append("i + ");
		stringbuilder.append(this.getY()).append("j + ");
		stringbuilder.append(this.getZ()).append("k]");
		return stringbuilder.toString();
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

	public float getZ() {
		return this.z;
	}

	public float getW() {
		return this.w;
	}

	private static float cos(float p_214904_0_) {
		return (float)Math.cos(p_214904_0_);
	}

	private static float sin(float p_214903_0_) {
		return (float)Math.sin(p_214903_0_);
	}
}
