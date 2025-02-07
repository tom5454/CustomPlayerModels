package com.tom.cpm.blockbench.proxy.three;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.MeshBasicMaterial")
public class MeshBasicMaterial extends ThreeMaterial {

	public String name;
	public ThreeTexture map;
	public float lineWidth;
	public ThreeColor color;

	public MeshBasicMaterial(MeshBasicMaterialInit init) {
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class MeshBasicMaterialInit {
		public int color, side;
		public boolean vertexColors, depthWrite, transparent;
		public float alphaTest;
		public ThreeTexture map;
	}
}
