package com.tom.cpm.web.client.fbxtool.three;

import com.tom.cpl.math.Quaternion;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.Quaternion")
public class ThreeQuat {
	public native void set(float x, float y, float z, float w);

	@JsOverlay
	public final void set(Quaternion q) {
		set(q.getX(), q.getY(), q.getZ(), q.getW());
	}
}
