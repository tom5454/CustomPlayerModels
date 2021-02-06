package com.tom.cpm.shared.math;

public class Box {
	public int x, y, w, h;

	public Box(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public boolean isInBounds(int x, int y) {
		return this.x < x && this.y < y && this.x+w > x && this.y+h > y;
	}
}
