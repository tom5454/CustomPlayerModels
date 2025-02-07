package com.tom.cpm.blockbench.proxy;

import com.tom.cpl.math.Vec3f;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_KeyframeDataPoint_$$")
public class KeyframeDataPoint {
	public Keyframe keyframe;
	public float x, y, z;

	@JsProperty(name = "cpm_visible")
	private boolean visibleB;

	@JsProperty(name = "cpm_visible")
	private String visibleS;

	@JsOverlay
	public final void set(Vec3f vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}

	@JsOverlay
	public final boolean getVisible() {
		if (Js.typeof(visibleS) == "string")return visibleS == "true";
		return visibleB;
	}

	@JsOverlay
	public final void setVisible(boolean visibleB) {
		this.visibleB = visibleB;
	}
}