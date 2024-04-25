package com.tom.ugwt.client;

import elemental2.core.JsObject;
import elemental2.dom.Window;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

public class UGWTContext {
	private static final Window runtimeContext = G.context;

	//UGWT won't replace unless in separate class
	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	private static class G {

		@JsProperty(name = "$$ugwt_m___ugwt_ctx___$$")
		public static Window context;

		@JsProperty(name = "$$ugwt_m__ugwtapp_$$")
		public static JsObject appScript;
	}

	public static void setContext(Window w) {
		G.context = w;
	}

	public static void resetContext() {
		G.context = runtimeContext;
	}

	public static String getAppScript() {
		return G.appScript.toString_();
	}
}
