package com.tom.cpm.web.client.fbxtool.three;

import com.tom.ugwt.client.JsArrayE;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.Object3D")
public class Object3D {
	public String name;
	public JsArrayE<Object3D> children;

	public native void add(Object3D b);
}
