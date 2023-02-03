package com.tom.cpm.web.client.util;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "navigator.clipboard")
public class Clipboard {
	public static native void writeText(String text);
}