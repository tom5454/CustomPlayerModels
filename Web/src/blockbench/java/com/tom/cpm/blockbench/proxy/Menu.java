package com.tom.cpm.blockbench.proxy;

import com.tom.ugwt.client.JsArrayE;

import elemental2.dom.EventTarget;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Menu_$$")
public class Menu {
	public JsArrayE<Object> structure;

	public Menu(Object[] opt) {}

	public native void open(EventTarget target);
}
