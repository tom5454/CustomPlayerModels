package com.tom.cpm.web.client.util;

import elemental2.core.ArrayBuffer;
import elemental2.dom.Blob;
import elemental2.promise.Promise;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class JSZip {

	public native JSZip file(String name, String content);
	public native ZipEntry file(String name);
	public native JSZip folder(String name);
	public native Promise<Blob> generateAsync(ZipWriteProperties arg);
	public native Promise<JSZip> loadAsync(ArrayBuffer content);
	public native void file(String name, String content, ZipFileProperties prop);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	public static class ZipEntry {
		public native Promise<String> async(String type);
	}

	public JsPropertyMap<ZipEntry> files;

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class ZipFileProperties {
		public boolean base64;

		@JsOverlay
		public static ZipFileProperties make() {
			ZipFileProperties v = new ZipFileProperties();
			v.base64 = true;
			return v;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class ZipWriteProperties {
		public String type, compression;
		public ZipCompressionOptions compressionOptions;

		@JsOverlay
		public static ZipWriteProperties make() {
			ZipWriteProperties v = new ZipWriteProperties();
			v.type = "blob";
			v.compression = "DEFLATE";
			v.compressionOptions = new ZipCompressionOptions();
			v.compressionOptions.level = 9;
			return v;
		}
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class ZipCompressionOptions {
		public int level;
	}
}
