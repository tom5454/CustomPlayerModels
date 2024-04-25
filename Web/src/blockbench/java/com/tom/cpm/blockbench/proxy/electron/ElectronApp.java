package com.tom.cpm.blockbench.proxy.electron;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "")
public class ElectronApp {
	public native void on(String event, ElectronEventHandler h);
	public native void removeListener(String event, ElectronEventHandler h);
	public native String getPath(String name);
	public native void focus();

	@JsFunction
	public static interface ElectronEventHandler {
		void onEvent(Object ev, BrowserWindow w);
	}
}