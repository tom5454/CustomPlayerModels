package com.tom.cpm.web.client.fbxtool.three;

import elemental2.dom.HTMLCanvasElement;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.WebGLRenderer")
public class WebGLRenderer {
	public HTMLCanvasElement domElement;
	public native void setSize(double w, double h);
	public native void render(Scene scene, PerspectiveCamera camera);
}
