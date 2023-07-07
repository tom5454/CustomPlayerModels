package com.tom.cpm.web.client.fbxtool.three;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.Bone")
public class Bone extends Object3D {
	public ThreeVec3 position, scale;
	public ThreeRot rotation;
	public ThreeQuat quaternion;

	@JsProperty(name = "_cpm_hidden_post")
	public boolean hidden;
}
