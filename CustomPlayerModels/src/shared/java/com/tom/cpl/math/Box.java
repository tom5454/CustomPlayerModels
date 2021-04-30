package com.tom.cpl.math;

public class Box {
	public int x, y, w, h;

	public Box(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public Box(Box b) {
		this(b.x, b.y, b.w, b.h);
	}

	public boolean isInBounds(Vec2i v) {
		return isInBounds(v.x, v.y);
	}

	public boolean isInBounds(int x, int y) {
		return this.x <= x && this.y <= y && this.x+w > x && this.y+h > y;
	}

	public boolean intersects(Box box) {
		return x < box.x+box.w && x+w > box.x && y < box.y+box.h && y+h > box.y;
	}

	public Box intersect(Box other) {
		int f0 = Math.max(this.x, other.x);
		int f1 = Math.max(this.y, other.y);
		int f3 = Math.min(this.x + w, other.x + other.w);
		int f4 = Math.min(this.y + h, other.y + other.h);
		return new Box(f0, f1, f3 - f0, f4 - f1);
	}
}
