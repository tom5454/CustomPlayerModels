package com.tom.cpm.blockbench.proxy;

import elemental2.core.JsObject;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Dialog_$$")
public class Dialog {

	public Dialog(DialogProperties ctr) {}

	public String[] lines;
	public CallbackConfirm onConfirm;
	public CallbackCancel onCancel;

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class DialogProperties {
		public String title, id;
		public JsObject form;
		public boolean draggable;
		public CallbackConfirm onConfirm;
		public CallbackCancel onCancel;
		public String[] lines;
		public boolean singleButton;
		public int width;
	}

	@JsFunction
	public interface CallbackConfirm {
		void confirm(JsObject result);
	}

	@JsFunction
	public interface CallbackCancel {
		void cancel();
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormElement {
		public String label, type;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormSelectElement extends FormElement {
		public Object value;
		public JsObject options;

		@JsOverlay
		public static FormSelectElement make() {
			FormSelectElement e = new FormSelectElement();
			e.type = "select";
			return e;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormFileElement extends FormElement {
		public String[] extensions;
		public String readtype, filetype;

		@JsOverlay
		public static FormFileElement make() {
			FormFileElement e = new FormFileElement();
			e.type = "file";
			return e;
		}
	}

	public native void show();
	public native void hide();
}
