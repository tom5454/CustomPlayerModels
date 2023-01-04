package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.blockbench.proxy.MenuBar.BarItem;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Toolbars_$$")
public class Toolbars {
	public static Toolbar outliner;
	public static Toolbar texturelist;
	public static Toolbar tools;
	public static Toolbar element_position;
	public static Toolbar element_size;
	public static Toolbar element_origin;
	public static Toolbar element_rotation;
	public static Toolbar palette;
	public static Toolbar color_picker;
	public static Toolbar display;
	public static Toolbar uv_editor;
	public static Toolbar animations;
	public static Toolbar keyframe;
	public static Toolbar timeline;
	public static Toolbar main_tools;
	public static Toolbar brush;
	public static Toolbar vertex_snap;
	public static Toolbar seam_tool;

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Toolbar_$$")
	public static class Toolbar {
		public BarItem[] children;
		public native void add(Action a, String pos);
	}
}
