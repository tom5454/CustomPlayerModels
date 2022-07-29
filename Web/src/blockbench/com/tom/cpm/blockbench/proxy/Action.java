package com.tom.cpm.blockbench.proxy;

import elemental2.dom.Event;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Action_$$")
public class Action {

	public Action(String name, ActionProperties ctr) {}

	public native void delete();

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class ActionProperties {
		public String name, description, icon, category;
		public CallbackClick click;
		public CallbackChildren children;
		public Condition condition;
	}

	@JsFunction
	public interface CallbackClick {
		void click(Event event);
	}

	@JsFunction
	public interface CallbackChildren {
		Action[] children();
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class Condition {
		public String[] formats;
	}
}
