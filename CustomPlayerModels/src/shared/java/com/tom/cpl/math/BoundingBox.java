package com.tom.cpl.math;

public class BoundingBox {
	public float minX;
	public float minY;
	public float minZ;
	public float maxX;
	public float maxY;
	public float maxZ;

	public BoundingBox(float x1, float y1, float z1, float x2, float y2, float z2) {
		this.minX = Math.min(x1, x2);
		this.minY = Math.min(y1, y2);
		this.minZ = Math.min(z1, z2);
		this.maxX = Math.max(x1, x2);
		this.maxY = Math.max(y1, y2);
		this.maxZ = Math.max(z1, z2);
	}

	public static BoundingBox create(float x, float y, float z, float w, float h, float d) {
		return new BoundingBox(x, y, z, x+w, y+h, z+d);
	}
}
