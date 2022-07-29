package com.tom.cpm.blockbench.proxy;

import elemental2.core.JsObject;
import elemental2.dom.HTMLCanvasElement;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_TextureGenerator_$$")
public class TextureGenerator {
	public static native void paintCubeBoxTemplate(Cube cube, Texture tex, HTMLCanvasElement canvas, JsObject template, boolean transparent);
}