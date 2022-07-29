package com.tom.cpm.blockbench.proxy;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_ModelFormat_$$")
public class ModelFormat {

	public ModelFormat(FormatProperties ctr) {}

	@JsProperty(name = "new")
	public CallbackNew new_;

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormatProperties {
		public String id, icon, name, description;
		public boolean bone_rig, box_uv, optional_box_uv, centered_grid, single_texture, rotate_cubes;
		public Codec codec;
	}

	@JsFunction
	public interface CallbackNew {
		boolean callNew();
	}
}
