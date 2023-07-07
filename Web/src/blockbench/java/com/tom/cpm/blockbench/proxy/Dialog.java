package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.blockbench.proxy.Action.Condition;
import com.tom.cpm.blockbench.proxy.jq.JQueryNode;

import elemental2.core.JsObject;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Dialog_$$")
public class Dialog {

	private static @JsOverlay final String FORM_CHECKBOX = "checkbox";
	private static @JsOverlay final String FORM_SELECT = "select";
	private static @JsOverlay final String FORM_TEXT = "text";
	private static @JsOverlay final String FORM_FILE = "file";
	private static @JsOverlay final String FORM_VECTOR = "vector";
	private static @JsOverlay final String FORM_INFO = "info";

	public Dialog(DialogProperties ctr) {}

	private String[] lines;
	private Object object;
	public CallbackConfirm onConfirm;
	public CallbackCancel onCancel;
	public VueComponent component;

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class DialogProperties {
		public String title, id;
		public JsObject form;
		public boolean draggable;
		public CallbackConfirm onConfirm;
		public CallbackCancel onCancel;
		public CallbackButton onButton;
		public CallbackFormChange onFormChange;
		public CallbackBuild onBuild;
		public String[] lines, buttons;
		public boolean singleButton;
		public int width, confirmIndex, cancelIndex;
		public VueComponent component;
	}

	@JsFunction
	public interface CallbackConfirm {
		boolean confirm(JsObject result);
	}

	@JsFunction
	public interface CallbackCancel {
		boolean cancel();
	}

	@JsFunction
	public interface CallbackButton {
		boolean onButton(int id);
	}

	@JsFunction
	public interface CallbackFormChange {
		void onChange(JsObject result);
	}

	@JsFunction
	public interface CallbackBuild {
		void onBuild();
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormElement {
		public String label, type, description;
		public Condition condition;
		public JQueryNode bar;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormElementBar {
		public native void toggle(boolean show);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormSelectElement extends FormElement {
		public Object value;
		public JsObject options;

		@JsOverlay
		public static FormSelectElement make(String label) {
			FormSelectElement e = new FormSelectElement();
			e.type = FORM_SELECT;
			e.label = label;
			return e;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormFileElement extends FormElement {
		public String[] extensions;
		public String readtype, filetype;

		@JsOverlay
		public static FormFileElement make(String label) {
			FormFileElement e = new FormFileElement();
			e.type = FORM_FILE;
			e.label = label;
			return e;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormCheckboxElement extends FormElement {
		public boolean value;

		@JsOverlay
		public static FormCheckboxElement make(String label) {
			FormCheckboxElement e = new FormCheckboxElement();
			e.type = FORM_CHECKBOX;
			e.label = label;
			return e;
		}

		@JsOverlay
		public static FormCheckboxElement make(String label, boolean v) {
			FormCheckboxElement e = new FormCheckboxElement();
			e.type = FORM_CHECKBOX;
			e.label = label;
			e.value = v;
			return e;
		}

		@JsOverlay
		public static FormCheckboxElement make(String label, boolean v, String desc) {
			FormCheckboxElement e = new FormCheckboxElement();
			e.type = FORM_CHECKBOX;
			e.label = label;
			e.value = v;
			e.description = desc;
			return e;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormTextboxElement extends FormElement {
		public String value;

		@JsOverlay
		public static FormTextboxElement make(String label) {
			FormTextboxElement e = new FormTextboxElement();
			e.type = FORM_TEXT;
			e.label = label;
			return e;
		}

		@JsOverlay
		public static FormTextboxElement make(String label, String v) {
			FormTextboxElement e = new FormTextboxElement();
			e.type = FORM_TEXT;
			e.label = label;
			e.value = v;
			return e;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormVectorElement extends FormElement {
		public float[] value;
		public int dimensions;
		public float min, max;

		@JsOverlay
		public static FormVectorElement make(String label, int dim) {
			FormVectorElement e = new FormVectorElement();
			e.type = FORM_VECTOR;
			e.label = label;
			e.dimensions = dim;
			e.value = new float[dim];
			return e;
		}

		@JsOverlay
		public static FormVectorElement make(String label, float... v) {
			FormVectorElement e = new FormVectorElement();
			e.type = FORM_VECTOR;
			e.label = label;
			e.dimensions = v.length;
			e.value = v;
			return e;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FormInfoElement extends FormElement {
		public String text;

		@JsOverlay
		public static FormInfoElement make(String label, String text, String desc) {
			FormInfoElement e = new FormInfoElement();
			e.type = FORM_INFO;
			e.label = label;
			e.text = text;
			e.description = desc;
			return e;
		}
	}

	public native void show();
	public native void hide();

	@JsOverlay
	public final void setLines(String... lines) {
		hide();
		this.lines = lines;
		this.object = null;
	}
}
