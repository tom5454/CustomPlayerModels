package com.tom.cpm.blockbench.proxy;

import elemental2.core.ArrayBuffer;
import elemental2.core.JsObject;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Codec_$$")
public class Codec {

	@JsConstructor
	public Codec(String id, CodecProperties ctr) {}

	public ModelFormat format;
	public String name, extension;
	public boolean overwrite;

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class CodecProperties {
		public String name, extension;
		public boolean remember;
		public LoadFilter load_filter;
		public CallbackCompile compile;
		public CallbackWrite write;
		public CallbackExport export;
		public CallbackParse parse;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class LoadFilter {
		public String type;
		public String[] extensions;
	}

	@JsFunction
	public interface CallbackCompile {
		String compile();
	}

	@JsFunction
	public interface CallbackWrite {
		void write(JsObject content, String path);
	}

	@JsFunction
	public interface CallbackExport {
		void export();
	}

	@JsFunction
	public interface CallbackParse {
		void parse(ArrayBuffer buffer, String path);
	}

	@JsFunction
	public interface CallbackOverwrite {
		void doOverwrite(String path);
	}

	public native void export();
	public native void dispatchEvent(String eventName, JsObject data);
	public native void afterDownload(String path);
	public native void afterSave(String path);
	public native void write(JsObject content, String path);
	public native String fileName();
	public native String startPath();
	public native void overwrite(JsObject content, String path, CallbackOverwrite cb);
}
