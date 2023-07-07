package com.tom.cpm.web.client.fbxtool.three;

import elemental2.core.Uint8Array;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "THREE.DataTexture")
public class DataTexture {

	public int minFilter, magFilter;
	public boolean needsUpdate;
	public String name;

	public DataTexture(Uint8Array pixels, int w, int h, int format) {
	}
}
