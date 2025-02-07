package com.tom.cpm.blockbench.proxy.three;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.Texture")
public class ThreeTexture {
	public int minFilter, magFilter;
	public boolean needsUpdate;
	public String name;
}
