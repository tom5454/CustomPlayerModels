package com.tom.cpm.shared.util;

import java.util.function.DoubleUnaryOperator;

public class RotationInterpolator implements DoubleUnaryOperator {
	private static final double TPI = 2 * Math.PI;
	private final double fullRot;
	private Double prevVal;
	private double mul;

	private RotationInterpolator(double fullRot) {
		this.fullRot = fullRot;
	}

	@Override
	public double applyAsDouble(double value) {
		if(prevVal == null) {
			prevVal = value;
			return value;
		} else {
			double v1 = Math.abs(value - prevVal);
			double v2 = Math.abs(fullRot - prevVal + value);
			double v3 = Math.abs(fullRot + prevVal - value);
			prevVal = value;
			if(v1 < v2 && v1 < v3) {
				return value + mul;
			} else if(v1 > v2 && v2 < v3) {
				mul += fullRot;
				return value + mul;
			} else if(v1 > v3 && v2 > v3) {
				mul -= fullRot;
				return value + mul;
			}
		}
		return value;
	}

	public static DoubleUnaryOperator createRad() {
		return new RotationInterpolator(TPI);
	}

	public static DoubleUnaryOperator createDeg() {
		return new RotationInterpolator(360);
	}

	/*public static void main(String[] args) {
		RotationInterpolator r = new RotationInterpolator();
		prln(r.applyAsDouble(Math.toRadians(350)));
		System.out.println("");
		prln(r.applyAsDouble(Math.toRadians(10)));

		System.out.println();
		System.out.println("==");
		System.out.println();
		r = new RotationInterpolator();
		prln(r.applyAsDouble(Math.toRadians(10)));
		System.out.println("");
		prln(r.applyAsDouble(Math.toRadians(350)));
	}

	private static void prln(double v) {
		System.out.println(Math.toDegrees(v));
	}*/
}
