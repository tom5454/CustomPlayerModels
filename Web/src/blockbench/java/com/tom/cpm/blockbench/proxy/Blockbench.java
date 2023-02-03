package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.blockbench.proxy.electron.Electron;

import elemental2.core.ArrayBuffer;
import elemental2.core.JsObject;
import elemental2.dom.Blob;
import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Blockbench_$$")
public class Blockbench {

	public static String version;

	@JsMethod(name = "import")
	public static native void import_(ImportProperties pr, CallbackImport cb);

	public static native void export(ExportProperties pr, CallbackExport cb);
	public static native void writeFile(String path, WriteProperties pr, CallbackExport cb);

	public static native void on(String id, CallbackEvent cb);
	public static native void removeListener(String id, CallbackEvent cb);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class ImportProperties {
		public String[] extensions;
		public String type, readtype, resource_id;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class ExportProperties {
		public String[] extensions;
		public String type, name, resource_id, startpath;

		@JsProperty(name = "content")
		public String textContent;

		@JsProperty(name = "content")
		public Blob binaryContent;

		public CallbackWriter custom_writer;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class WriteProperties {
		public JsObject content;
		public String savetype;
	}

	@JsFunction
	public interface CallbackImport {
		void doImport(BBFile[] files);
	}

	@JsFunction
	public interface CallbackExport {
		void doExport(String path);
	}

	@JsFunction
	public interface CallbackWriter {
		void doWrite(JsObject content, String path);
	}

	@JsFunction
	public interface CallbackEvent {
		void onEvent(Object data);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class BBFile {
		public String name, path;

		@JsProperty(name = "content")
		public String textContent;

		@JsProperty(name = "content")
		public ArrayBuffer binaryContent;
	}

	@JsOverlay
	public static void focus() {
		if(Electron.isElectron())Electron.app.focus();
		else DomGlobal.window.focus();
	}
}
