package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.blockbench.proxy.MenuBar.BarItem;

import elemental2.dom.Event;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Action_$$")
public class Action extends BarItem {

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
		public String[] modes;
		public String[] tools;
		public String[] features;
		public boolean project;
		public ConditionMethod method;
	}

	@JsFunction
	public interface ConditionMethod {
		boolean check(Object context);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class ToggleProperties extends ActionProperties {
		public ToggleOnChange onChange;

		@JsProperty(name = "default")
		public boolean def;
	}

	@JsFunction
	public interface ToggleOnChange {
		void onChange(boolean newValue);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Toggle_$$")
	public static class Toggle extends Action {
		public boolean value;

		public Toggle(String name, ActionProperties ctr) {
			super(name, ctr);
		}

		public native void updateEnabledState();
	}
}
