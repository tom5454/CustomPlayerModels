package com.tom.cpm.web.client.fbxtool.three;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.SkinnedMesh")
public class SkinnedMesh extends Mesh {

	public SkinnedMesh(Geometry geometry, MeshBasicMaterial material) {
		super(geometry, material);
	}

	public native void bind(Skeleton arm);
}
