package com.tom.cpm.shared.math;

public class Vec2i {
	public int x, y;

	public Vec2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Vec2i(float x, float y) {
		this.x = (int) x;
		this.y = (int) y;
	}

	public Vec2i(Vec2i v) {
		this.x = v.x;
		this.y = v.y;
	}

	public Vec2i() {
		this.x = 0;
		this.y = 0;
	}
}
