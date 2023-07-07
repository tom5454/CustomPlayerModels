package com.tom.cpm.blockbench.proxy.three;

import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ThreeVec3 {
	public double x, y, z;

	@JsOverlay
	public final void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@JsOverlay
	public final void set(Vec3f v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	@JsOverlay
	public final void set(Vec4f v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	@JsOverlay
	public final void set(float v) {
		this.x = v;
		this.y = v;
		this.z = v;
	}

	@JsMethod(name = "clone")
	public native ThreeVec3 copy();
}
