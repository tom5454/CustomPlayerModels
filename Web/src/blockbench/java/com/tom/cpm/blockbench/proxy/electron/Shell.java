package com.tom.cpm.blockbench.proxy.electron;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "")
public class Shell {
	public native void openExternal(String path);
}
