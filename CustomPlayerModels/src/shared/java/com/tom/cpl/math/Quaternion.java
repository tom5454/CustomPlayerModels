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

	public Quaternion(float x, float y, float z, RotationOrder order) {
		this(x, y, z, order, true);
	}

	public Quaternion(float x, float y, float z, RotationOrder order, boolean degrees) {
		if (degrees) {
			x *= ((float)Math.PI / 180F);
			y *= ((float)Math.PI / 180F);
			z *= ((float)Math.PI / 180F);
		}

		float l = cos(0.5F * x);
		float c = cos(0.5F * y);
		float h = cos(0.5F * z);
		float u = sin(0.5F * x);
		float d = sin(0.5F * y);
		float p = sin(0.5F * z);

		switch (order) {
		case XYZ:
			this.x = u * c * h + l * d * p;
			this.y = l * d * h - u * c * p;
			this.z = l * c * p + u * d * h;
			this.w = l * c * h - u * d * p;
			break;

		case ZXY:
			this.x = u * c * h - l * d * p;
			this.y = l * d * h + u * c * p;
			this.z = l * c * p + u * d * h;
			this.w = l * c * h - u * d * p;
			break;

		case ZYX:
			this.x = u * c * h - l * d * p;
			this.y = l * d * h + u * c * p;
			this.z = l * c * p - u * d * h;
			this.w = l * c * h + u * d * p;
			break;

		default:
			break;
		}
	}

	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Quaternion(Mat4f m) {
		double w = Math.sqrt(1 + m.m00 + m.m11 + m.m22) / 2;
		this.x = (float) ((m.m21 - m.m12) / (4 * w));
		this.y = (float) ((m.m02 - m.m20) / (4 * w));
		this.z = (float) ((m.m10 - m.m01) / (4 * w));
		this.w = (float) w;
	}

	public Quaternion(Vec3f zyx, boolean degrees) {
		this(zyx.x, zyx.y, zyx.z, RotationOrder.ZYX, degrees);
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

	public void mul(Quaternion other) {
		float f = this.getX();
		float f1 = this.getY();
		float f2 = this.getZ();
		float f3 = this.getW();
		float f4 = other.getX();
		float f5 = other.getY();
		float f6 = other.getZ();
		float f7 = other.getW();
		this.x = f3 * f4 + f * f7 + f1 * f6 - f2 * f5;
		this.y = f3 * f5 - f * f6 + f1 * f7 + f2 * f4;
		this.z = f3 * f6 + f * f5 - f1 * f4 + f2 * f7;
		this.w = f3 * f7 - f * f4 - f1 * f5 - f2 * f6;
	}

	public static Vec3f matrixToRotation(Mat4f m, RotationOrder to) {
		float a = m.m02;
		float c = m.m12;
		float d = m.m22;
		float s = m.m01;
		float r = m.m00;
		float u = m.m21;
		float l = m.m11;
		float h = m.m20;
		float o = m.m10;
		switch (to) {
		case XYZ:
		{
			float x, y, z;
			y = (float) Math.asin(MathHelper.clamp(a, -1, 1));
			if(Math.abs(a) < .9999999) {
				x = (float) Math.atan2(-c, d);
				z = (float) Math.atan2(-s, r);
			} else {
				x = (float) Math.atan2(u, l);
				z = 0;
			}
			x = (float) Math.toDegrees(x);
			y = (float) Math.toDegrees(y);
			z = (float) Math.toDegrees(z);
			return new Vec3f(x, y, z);
		}

		case ZXY:
		{
			float x, y, z;
			x = (float) Math.asin(MathHelper.clamp(u, -1, 1));
			if(Math.abs(u) < .9999999) {
				y = (float) Math.atan2(-h, d);
				z = (float) Math.atan2(-s, l);
			} else {
				z = (float) Math.atan2(o, r);
				y = 0;
			}
			x = (float) Math.toDegrees(x);
			y = (float) Math.toDegrees(y);
			z = (float) Math.toDegrees(z);
			return new Vec3f(x, y, z);
		}

		case ZYX:
		{
			float x, y, z;
			y = (float) Math.asin(-MathHelper.clamp(h, -1, 1));
			if(Math.abs(h) < .9999999) {
				x = (float) Math.atan2(u, d);
				z = (float) Math.atan2(o, r);
			} else {
				z = (float) Math.atan2(-s, l);
				x = 0;
			}
			x = (float) Math.toDegrees(x);
			y = (float) Math.toDegrees(y);
			z = (float) Math.toDegrees(z);
			return new Vec3f(x, y, z);
		}

		default:
			break;
		}
		return new Vec3f();
	}

	public static Vec3f reorder(Vec3f in, RotationOrder orderIn, RotationOrder to) {
		return matrixToRotation(new Mat4f(new Quaternion(in.x, in.y, in.z, orderIn)), to);
	}

	public static enum RotationOrder {
		ZYX,
		ZXY,
		XYZ
	}
}
