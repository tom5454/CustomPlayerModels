package com.tom.cpm.web.client.fbxtool.three;

import com.tom.ugwt.client.JsArrayE;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.Raycaster")
public class Raycaster {
	public native void setFromCamera(ThreeVec2 v, PerspectiveCamera camera);
	public native JsArrayE<RayResult> intersectObject(Object3D obj);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class RayResult {
		public int index;
		public Object3D object;
		public ThreeVec3 point;
	}
}
