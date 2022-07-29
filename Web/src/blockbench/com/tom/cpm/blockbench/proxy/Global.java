package com.tom.cpm.blockbench.proxy;

import com.tom.ugwt.client.JsArrayE;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_window_$$")
public class Global {
	public static native boolean newProject(ModelFormat format);
	public static native void loadTextureDraggable();
	public static native void setProjectTitle();
	public static native void updateSelection();

	@JsMethod(name = "tl")
	public static native String translate(String key);

	//UGWT won't replace unless in separate class
	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	private static class G {

		@JsProperty(name = "$$ugwt_m_isApp_$$")
		public static boolean isApp;

		@JsProperty(name = "$$ugwt_m_getAllGroups_$$")
		public static native JsArrayE<Group> getAllGroups();
	}

	@JsOverlay
	public static boolean isApp() {
		return G.isApp;
	}

	@JsOverlay
	public static JsArrayE<Group> getAllGroups() {
		return G.getAllGroups();
	}
}
