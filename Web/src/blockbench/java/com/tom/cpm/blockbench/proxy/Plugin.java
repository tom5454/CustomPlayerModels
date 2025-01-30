package com.tom.cpm.blockbench.proxy;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Plugin_$$")
public class Plugin {
	public String id;

	public static native void register(String id, PluginProperties pr);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class PluginProperties {
		public String name, author, description, icon, version, variant, min_version;
		public String[] tags;
		public Callback onload, onunload;
	}

	@JsFunction
	public interface Callback {
		void run();
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Plugins_$$")
	public static class Plugins {

		public static Plugin[] all;
	}

	public native void reload();
}
