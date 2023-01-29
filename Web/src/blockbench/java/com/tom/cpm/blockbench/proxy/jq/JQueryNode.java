package com.tom.cpm.blockbench.proxy.jq;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
public class JQueryNode {
	public native JQueryNode toggle(boolean show);
	public native JQueryNode find(String querry);
	public native JQueryNode prop(String key, Object value);
	public native JQueryNode css(String style, Object value);

	@JsOverlay
	public final JQueryNode disabled(boolean d) {
		prop("disabled", d);
		css("color", d ? "var(--color-button)" : "");
		return this;
	}
}
