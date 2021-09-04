package com.tom.cpm.shared.gui;

import com.tom.cpl.math.Vec3f;

public class ViewportCamera {
	public Vec3f position = new Vec3f(0.5f, 1, 0.5f);
	public Vec3f look = new Vec3f(0.25f, 0.5f, 0.25f);
	public float camDist = 64;
}