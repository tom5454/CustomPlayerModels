package com.tom.cpm.web.client.fbxtool.three;

import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.Vector3")
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

	public native double distanceTo(ThreeVec3 v);
}
