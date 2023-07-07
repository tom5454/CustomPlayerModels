package com.tom.cpm.web.client.fbxtool.three;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.BufferGeometry")
public class BufferGeometry extends Geometry {
	public native void setIndex(BufferAttribute ind);
}
