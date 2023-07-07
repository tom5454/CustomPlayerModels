package com.tom.cpm.blockbench.util;

import com.tom.cpm.blockbench.proxy.VueComponent;

import jsinterop.annotations.JsFunction;

public class VueUtil {
	public static native void setMounted(VueComponent v, MountCallback cb)/*-{
		v.mounted = function() { cb(this); }
	}-*/;

	@JsFunction
	public static interface MountCallback {
		void call(VueComponent comp);
	}
}
