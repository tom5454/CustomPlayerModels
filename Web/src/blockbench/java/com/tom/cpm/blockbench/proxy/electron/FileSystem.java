package com.tom.cpm.blockbench.proxy.electron;

import elemental2.core.JsObject;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class FileSystem {
	public native boolean existsSync(String path);
	public native String[] readdirSync(String path);
	public native Buffer readFileSync(String path);
	public native JsObject readFileSync(String path, String mode);
	public native void readFile(String path, ReadCallback cb);
	public native void writeFile(String path, Buffer content, WriteCallback cb);
	public native void mkdirSync(String path, MkDirOpt opt);
	public native FSStats lstatSync(String path);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	public static class Buffer {

		public static native Buffer from(String content, String type);

		public native String toString(String mode);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_fs.Stats_$$")
	public static class FSStats {
		public native boolean isDirectory();
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class MkDirOpt {
		public boolean recursive;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class ReadFileOpt {
		public String encoding, flag;
	}

	@JsFunction
	public interface ReadCallback {
		void run(Object error, Buffer data);
	}

	@JsFunction
	public interface WriteCallback {
		void run(Object error);
	}

	@JsOverlay
	public final void mkdirs(String path) {
		MkDirOpt o = new MkDirOpt();
		o.recursive = true;
		mkdirSync(path, o);
	}
}
