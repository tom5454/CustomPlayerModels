package com.tom.cpm.blockbench.proxy.three;

import com.tom.ugwt.client.JsArrayE;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.Object3D")
public class Object3D {
	public String name;
	public JsArrayE<Object3D> children;

	public ThreeVec3 position, scale, fix_position;
	public ThreeRot rotation, fix_rotation;
	public boolean visible;

	public native void add(Object3D b);
}
