package com.tom.cpm.web.client.fbxtool.three;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.PerspectiveCamera")
public class PerspectiveCamera {
	public ThreeVec3 position;
	public ThreeRot rotation;
	public float aspect;
	public PerspectiveCamera(float fov, double asp, float near, float far) {
	}
}
