package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.blockbench.proxy.Blockbench.CallbackEvent;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_NodePreviewController_$$")
public class NodePreviewController {

	public native void on(String id, CallbackEvent cb);
	public native void removeListener(String id, CallbackEvent cb);

	public native void updateGeometry(Object obj);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class NodePreviewEvent {
		public OutlinerElement element;

		@JsOverlay
		public static OutlinerElement getElement(Object evt) {
			NodePreviewEvent ev = Js.uncheckedCast(evt);
			return ev.element;
		}
	}
}
