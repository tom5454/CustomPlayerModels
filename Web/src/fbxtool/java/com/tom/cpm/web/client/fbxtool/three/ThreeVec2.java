package com.tom.cpm.web.client.fbxtool.three;

import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.Vector2")
public class ThreeVec2 {
	public double x, y;

	@JsOverlay
	public final void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@JsOverlay
	public final void set(Vec3f v) {
		this.x = v.x;
		this.y = v.y;
	}

	@JsOverlay
	public final void set(Vec4f v) {
		this.x = v.x;
		this.y = v.y;
	}

	@JsOverlay
	public final void set(float v) {
		this.x = v;
		this.y = v;
	}
}
