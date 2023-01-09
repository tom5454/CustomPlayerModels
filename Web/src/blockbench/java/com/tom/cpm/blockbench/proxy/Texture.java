package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.blockbench.proxy.three.ThreeMaterial;
import com.tom.ugwt.client.JsArrayE;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Texture_$$")
public class Texture {
	public static JsArrayE<Texture> all;
	public String name, source;

	public Texture() {}
	public Texture(TextureProperties ctr) {}

	public native Texture fromDataURL(String url);
	public native void add(boolean undo);

	public native ThreeMaterial getMaterial();

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class TextureProperties {
		public String mode, name;
	}
}
