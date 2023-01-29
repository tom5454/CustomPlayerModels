package com.tom.cpm.blockbench.proxy;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_ModelFormat_$$")
public class ModelFormat {

	public ModelFormat(FormatProperties ctr) {}

	@JsProperty(name = "new")
	public CallbackNew new_;
	public String name;

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormatProperties {
		public String id, icon, name, description, target, category;
		public boolean bone_rig, box_uv, optional_box_uv, centered_grid, single_texture, rotate_cubes, animation_mode, animation_controllers, animation_files, bone_binding_expression, uv_rotation;
		public Codec codec;
		public FormatPage format_page;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormatPage {
		public FormatPageContent[] content;

		@JsOverlay
		public static FormatPage create(FormatPageContent... contents) {
			FormatPage p = new FormatPage();
			p.content = contents;
			return p;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormatPageContent {
		public String type, text;

		@JsOverlay
		public static FormatPageContent create(String text) {
			FormatPageContent p = new FormatPageContent();
			p.text = text;
			return p;
		}

		@JsOverlay
		public static FormatPageContent create(String type, String text) {
			FormatPageContent p = new FormatPageContent();
			p.text = text;
			p.type = type;
			return p;
		}
	}

	@JsFunction
	public interface CallbackNew {
		boolean callNew();
	}
}
