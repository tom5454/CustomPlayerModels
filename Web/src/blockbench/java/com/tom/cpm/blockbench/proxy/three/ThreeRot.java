package com.tom.cpm.blockbench.proxy.three;

import com.tom.cpl.math.Vec3f;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ThreeRot {
	public float x, y, z;

	public native void set(float x, float y, float z, String order);

	@JsOverlay
	public final void set(Vec3f v) {
		set(v.x, v.y, v.z, "ZYX");
	}

	@JsMethod(name = "clone")
	public native ThreeRot copy();
}
