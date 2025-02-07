package com.tom.cpm.blockbench.proxy.three;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
public class ThreeColor {
	public float r, g, b;

	@JsOverlay
	public static ThreeColor make(int color) {
		ThreeColor c = new ThreeColor();
		c.r = ((color & 0xff0000) >> 16) / 255f;
		c.g = ((color & 0x00ff00) >> 8) / 255f;
		c.b = (color & 0x0000ff) / 255f;
		return c;
	}

	@JsOverlay
	public static ThreeColor make(float r, float g, float b) {
		ThreeColor c = new ThreeColor();
		c.r = r;
		c.g = g;
		c.b = b;
		return c;
	}
}
