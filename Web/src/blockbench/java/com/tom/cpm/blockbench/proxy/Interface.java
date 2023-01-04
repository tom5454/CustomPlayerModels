package com.tom.cpm.blockbench.proxy;

import elemental2.dom.HTMLDivElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Interface_$$")
public class Interface {
	public static HTMLDivElement work_screen;

	public static native void definePanels(DefineFunction func);

	@JsOverlay
	public static void defineBarActions(DefineFunction func) {
		G.BARS.defineActions(func);
	}

	@JsFunction
	public interface DefineFunction {
		void define();
	}

	@JsOverlay
	public static void updateInterface() {
		G.updateInterface();
	}

	//UGWT won't replace unless in separate class
	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	private static class G {

		@JsProperty(name = "$$ugwt_m_BARS_$$")
		public static Bars BARS;

		@JsMethod(name = "$$ugwt_m_updateInterface_$$")
		public static native void updateInterface();

	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class Bars {
		public native void defineActions(DefineFunction func);
	}
}
