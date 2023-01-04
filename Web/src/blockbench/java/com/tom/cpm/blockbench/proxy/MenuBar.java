package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.blockbench.proxy.Action.Condition;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_MenuBar_$$")
public class MenuBar {
	public static native void addAction(Action a, String id);

	public static JsPropertyMap<BarMenu> menus;

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_BarItem_$$")
	public static class BarItem {
		public String id;
		public Condition condition;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_BarMenu_$$")
	public static class BarMenu extends Menu {
		public BarMenu(String id, Object[] content, BarMenuInit options) {
			super(content);
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class BarMenuInit {
		public Condition condition;
		public String name;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class SubMenu {
		public String name, description, icon, id;
		public Object[] children;
	}
}
