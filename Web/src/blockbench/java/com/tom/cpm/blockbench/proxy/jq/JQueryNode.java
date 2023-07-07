package com.tom.cpm.blockbench.proxy.jq;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
public class JQueryNode {

	public int length;

	@JsMethod(name = "$", namespace = "window")
	public static native JQueryNode jq(String selector);

	@JsMethod(name = "$", namespace = "window")
	public static native JQueryNode jq(HTMLElement selector);

	public native JQueryNode toggle(boolean show);
	public native JQueryNode find(String querry);
	public native JQueryNode prop(String key, Object value);
	public native JQueryNode css(String style, Object value);
	public native JQueryNode html(String html);
	public native JQueryNode text(String text);

	@JsOverlay
	public final JQueryNode disabled(boolean d) {
		prop("disabled", d);
		css("color", d ? "var(--color-button)" : "");
		return this;
	}

	@JsOverlay
	public final HTMLElement getAt(int index) {
		HTMLElement[] ar = Js.uncheckedCast(this);
		return ar[index];
	}

	public native JQueryNode spectrum(String param, String value);
	public native JQueryNode spectrum(SpectrumInit init);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class SpectrumInit {
		public String preferredFormat;
		public String color;
		public boolean showAlpha;
		public boolean showInput;
		public SpectrumCallback move;
		public SpectrumCallback change;
		public SpectrumCallback hide;
	}

	@JsFunction
	public static interface SpectrumCallback {
		void call(TinyColor color);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class TinyColor {
		public native String toHexString();
	}
}
