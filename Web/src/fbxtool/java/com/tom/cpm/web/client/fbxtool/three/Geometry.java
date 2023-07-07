package com.tom.cpm.web.client.fbxtool.three;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.Geometry")
public class Geometry {

	public native void setAttribute(String id, BufferAttribute attr);
}
