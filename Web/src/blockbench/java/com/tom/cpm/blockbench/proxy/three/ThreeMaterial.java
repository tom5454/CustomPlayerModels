package com.tom.cpm.blockbench.proxy.three;

import com.tom.cpm.blockbench.proxy.three.MeshBasicMaterial.MeshBasicMaterialInit;

import elemental2.webgl.WebGLRenderingContext;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_THREE.ShaderMaterial_$$")
public class ThreeMaterial {

	@JsProperty(name = "cpm_glowMaterial")
	private ThreeMaterial glowMat;

	@JsProperty(name = "cpm_normMaterial")
	private ThreeMaterial normMat;

	@JsProperty(name = "cpm_origMaterial")
	protected ThreeMaterial origMat;

	public JsPropertyMap<ThreeUniform<?>> uniforms;

	private int blendSrc, blendDst, blendSrcAlpha, blendDstAlpha, blending;

	@JsMethod(name = "clone")
	public native ThreeMaterial copy();

	@JsOverlay
	public final ThreeMaterial getNormal() {
		if(normMat == null)normMat = this;
		return normMat;
	}

	@JsOverlay
	public final ThreeMaterial getOriginal() {
		if(origMat == null)return this;
		return origMat;
	}

	@JsOverlay
	public final ThreeMaterial getGlow() {
		if(glowMat == null) {
			MeshBasicMaterialInit mbmi = new MeshBasicMaterialInit();
			mbmi.alphaTest = 0.5f;
			mbmi.side = 2;
			mbmi.transparent = true;
			if (isColor()) {
				mbmi.map = ((MeshBasicMaterial) this).map;
			} else {
				mbmi.map = Js.uncheckedCast(uniforms.get("map").value);
			}
			MeshBasicMaterial m = new MeshBasicMaterial(mbmi);
			if (isColor()) {
				m.color = ((MeshBasicMaterial) this).color;
				m.origMat = origMat;
			}
			glowMat = m;
			glowMat.blendSrc = (int) WebGLRenderingContext.ONE;
			glowMat.blendDst = (int) WebGLRenderingContext.ONE;
			glowMat.glowMat = glowMat;
			glowMat.normMat = this;
			glowMat.blending = 2;
		}
		return glowMat;
	}

	@JsOverlay
	public final MeshBasicMaterial makeRecolor(int color, boolean colorOnly) {
		MeshBasicMaterialInit mbmi = new MeshBasicMaterialInit();
		mbmi.alphaTest = 0.5f;
		mbmi.side = 2;
		mbmi.transparent = true;
		mbmi.color = color;
		if (!colorOnly)mbmi.map = Js.uncheckedCast(uniforms.get("map").value);
		MeshBasicMaterial m = new MeshBasicMaterial(mbmi);
		m.origMat = this;
		return m;
	}

	@JsOverlay
	public final boolean isColor() {
		return origMat != null;
	}
}
