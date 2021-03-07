package com.tom.cpm.shared.math;

public class Box {
	public int x, y, w, h;

	public Box(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
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
}
