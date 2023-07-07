package com.tom.cpm.blockbench.proxy;

import com.tom.cpl.math.Vec3f;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_KeyframeDataPoint_$$")
public class KeyframeDataPoint {
	public Keyframe keyframe;
	public float x, y, z;

	@JsProperty(name = "cpm_visible")
	public boolean visible;

	@JsOverlay
	public final void set(Vec3f vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}
}