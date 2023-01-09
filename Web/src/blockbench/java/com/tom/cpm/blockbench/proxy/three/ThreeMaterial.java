package com.tom.cpm.blockbench.proxy.three;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_THREE.ShaderMaterial_$$")
public class ThreeMaterial {

	@JsProperty(name = "cpm_glowMaterial")
	private ThreeMaterial glowMat;

	@JsProperty(name = "cpm_normMaterial")
	private ThreeMaterial normMat;

	private int blendSrc, blendDst, blending;

	@JsMethod(name = "clone")
	public native ThreeMaterial copy();

	@JsOverlay
	public final ThreeMaterial getNormal() {
		if(normMat == null)normMat = this;
		return normMat;
	}

	@JsOverlay
	public final ThreeMaterial getGlow() {
		if(glowMat == null) {
			glowMat = copy();
			glowMat.blendSrc = 201;
			glowMat.blendDst = 201;
			glowMat.glowMat = glowMat;
			glowMat.normMat = this;
			glowMat.blending = 5;
		}
		return glowMat;
	}
}
