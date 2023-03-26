package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.blockbench.proxy.Action.Condition;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Property_$$")
public class Property {

	private Property(Object mp, String type, String id, PropertyInit init) {}

	public Condition condition;

	//UGWT won't replace unless in separate class
	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	private static class G {

		@JsProperty(name = "$$ugwt_m_ModelProject_$$")
		public static Object modelProject;

		@JsProperty(name = "$$ugwt_m_Group_$$")
		public static Object group;

		@JsProperty(name = "$$ugwt_m_Cube_$$")
		public static Object cube;

		@JsProperty(name = "$$ugwt_m_Locator_$$")
		public static Object locator;

		@JsProperty(name = "$$ugwt_m_Face_$$")
		public static Object face;

		@JsProperty(name = "$$ugwt_m_Texture_$$")
		public static Object texture;

		@JsProperty(name = "$$ugwt_m_Animation_$$")
		public static Object animation;
	}

	@JsOverlay
	public static Property createProperty(Clazz clz, Type type, String id, String label, Object def, Condition cond, boolean hidden) {
		PropertyInit init = new PropertyInit();
		init.label = label;
		init.def = def;
		init.exposed = !hidden;
		init.condition = cond;
		return new Property(clz.obj, type.name().toLowerCase(), id, init);
	}

	public static enum Clazz {
		PROJECT(G.modelProject),
		GROUP(G.group),
		CUBE(G.cube),
		LOCATOR(G.locator),
		FACE(G.face),
		TEXTURE(G.texture),
		ANIMATION(G.animation),
		;
		private Object obj;
		private Clazz(Object obj) {
			this.obj = obj;
		}
	}

	public static enum Type {
		STRING,
		MOLANG,
		NUMBER,
		BOOLEAN,
		ARRAY,
		INSTANCE,
		VECTOR,
		VECTOR2,
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class PropertyInit {
		public String label;

		@JsProperty(name = "default")
		public Object def;

		public Condition condition;
		public boolean exposed;
	}

	public native void delete();
}