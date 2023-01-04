package com.tom.cpm.blockbench.proxy;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Project_$$")
public class Project {
	public static int texture_width, texture_height;
	public static String name, geometry_name;
	public static boolean box_uv;

	@JsProperty(name = "cpm_data")
	public static String pluginData;

	@JsProperty(name = "cpm_hideHeadIfSkull")
	public static boolean hideHeadIfSkull;

	@JsProperty(name = "cpm_removeBedOffset")
	public static boolean removeBedOffset;

	//UGWT won't replace unless in separate class
	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	private static class G {

		@JsProperty(name = "$$ugwt_m_Project_$$")
		public static Object project;
	}

	@JsOverlay
	public static Object getProject() {
		return G.project;
	}
}
