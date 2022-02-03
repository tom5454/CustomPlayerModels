package com.tom.cpl.math;

public class Quaternion {
	private float x;
	private float y;
	private float z;
	private float w;
	private Vec3f axis;
	private float angle;

	public Quaternion(Vec3f axis, float angle, boolean degrees) {
		if (degrees) {
			angle *= ((float)Math.PI / 180F);
		}

		float f = sin(angle / 2.0F);
		this.x = axis.x * f;
		this.y = axis.y * f;
		this.z = axis.z * f;
		this.w = cos(angle / 2.0F);
		this.axis = axis;
		this.angle = angle;
	}

	public Quaternion(float x, float y, float z, boolean degrees) {
		if (degrees) {
			x *= ((float)Math.PI / 180F);
			y *= ((float)Math.PI / 180F);
			z *= ((float)Math.PI / 180F);
		}

		float f = sin(0.5F * x);
		float f1 = cos(0.5F * x);
		float f2 = sin(0.5F * y);
		float f3 = cos(0.5F * y);
		float f4 = sin(0.5F * z);
		float f5 = cos(0.5F * z);
		this.x = f * f3 * f5 + f1 * f2 * f4;
		this.y = f1 * f2 * f5 - f * f3 * f4;
		this.z = f * f2 * f5 + f1 * f3 * f4;
		this.w = f1 * f3 * f5 - f * f2 * f4;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other != null && this.getClass() == other.getClass()) {
			Quaternion quaternion = (Quaternion)other;
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

	private static float cos(float v) {
		return (float)Math.cos(v);
	}

	private static float sin(float v) {
		return (float)Math.sin(v);
	}

	public float getAngle() {
		return angle;
	}

	public Vec3f getAxis() {
		return axis;
	}

	@FunctionalInterface
	public static interface Qmap<T> {
		T apply(float x, float y, float z, float w);
	}

	public <T> T map(Qmap<T> map) {
		return map.apply(x, y, z, w);
	}
}
