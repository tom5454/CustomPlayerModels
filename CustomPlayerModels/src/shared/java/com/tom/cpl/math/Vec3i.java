package com.tom.cpl.math;

public class Vec3i {
	public int x, y, z;

	public Vec3i() {
	}

	public Vec3i(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3i(Vec3i v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
}