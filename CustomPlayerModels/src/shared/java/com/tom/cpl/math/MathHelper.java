package com.tom.cpl.math;

public class MathHelper {
	public static final double LOG2 = Math.log(2);
	public static final float PI = (float) Math.PI;

	public static double clamp(double num, double min, double max) {
		if (num < min) {
			return min;
		} else {
			return num > max ? max : num;
		}
	}

	public static float clamp(float num, float min, float max) {
		if (num < min) {
			return min;
		} else {
			return num > max ? max : num;
		}
	}

	/**
	 * Returns the value of the first parameter, clamped to be within the lower and upper limits given by the second and
	 * third parameters.
	 */
	public static int clamp(int num, int min, int max) {
		if (num < min) {
			return min;
		} else {
			return num > max ? max : num;
		}
	}

	public static int ceil(float value) {
		int i = (int)value;
		return value > i ? i + 1 : i;
	}

	public static int ceil(double value) {
		int i = (int)value;
		return value > i ? i + 1 : i;
	}

	public static float round(float number, int scale) {
		int pow = 1;
		for (int i = 0; i < scale; i++)
			pow *= 10;
		float tmp = number * pow;
		return ( (float) ( (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) ) ) / pow;
	}

	public static float fastInvCubeRoot(float number) {
		int i = Float.floatToIntBits(number);
		i = 1419967116 - i / 3;
		float f = Float.intBitsToFloat(i);
		f = 0.6666667F * f + 1.0F / (3.0F * f * f * number);
		return 0.6666667F * f + 1.0F / (3.0F * f * f * number);
	}

	public static double log2(double v) {
		return Math.log(v) / LOG2;
	}

	public static float lerp(float partial, float prev, float current) {
		return prev + partial * (current - prev);
	}

	public static float trigInt(float partial, float prev, float current) {
		float v = (float) Math.sin(partial * PI / 2);
		return prev + v * (current - prev);
	}

	public static float cos(float f) {
		return (float) Math.cos(f);
	}

	public static float sin(float f) {
		return (float) Math.sin(f);
	}

	public static float sqrt(float f) {
		return (float) Math.sqrt(f);
	}

	public static int floor(float value) {
		int i = (int)value;
		return value < i ? i - 1 : i;
	}

	public static int floor(double value) {
		int i = (int)value;
		return value < i ? i - 1 : i;
	}
}
