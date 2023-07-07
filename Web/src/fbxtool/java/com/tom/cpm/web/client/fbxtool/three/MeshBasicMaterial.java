package com.tom.cpm.web.client.fbxtool.three;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.MeshBasicMaterial")
public class MeshBasicMaterial {

	public String name;
	public DataTexture map;
	public float lineWidth;

	public MeshBasicMaterial(MeshBasicMaterialInit init) {
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class MeshBasicMaterialInit {
		public int color, side;
		public boolean vertexColors, depthWrite;
		public float alphaTest;
	}
}
