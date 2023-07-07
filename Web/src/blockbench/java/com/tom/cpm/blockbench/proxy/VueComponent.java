package com.tom.cpm.blockbench.proxy;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
public class VueComponent {
	public String template;
	public JsPropertyMap<VueCallback> methods;
	public JsPropertyMap<VueComponent> components;
	public JsPropertyMap<String> data;
	public JsPropertyMap<HTMLElement> $refs;

	@JsFunction
	public static interface VueCallback {
		void call();
	}
}
