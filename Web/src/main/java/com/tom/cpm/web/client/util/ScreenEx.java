package com.tom.cpm.web.client.util;

import elemental2.dom.Screen;
import elemental2.promise.Promise;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "screen")
public class ScreenEx extends Screen {
	public ScreenOrientation orientation;

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	public static class ScreenOrientation {
		public String type;
		public native Promise<?> lock(String type);
		public native void unlock();
	}
}
