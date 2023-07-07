package com.tom.cpm.web.client.fbxtool.three;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.Skeleton")
public class Skeleton {
	public Bone[] bones;

	public Skeleton(Bone[] bones) {
	}
}
