package com.tom.cpm.blockbench.proxy.electron;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class FileSystem {
	public native boolean existsSync(String path);
	public native void writeFileSync(String name, Buffer content);
	public native String[] readdirSync(String path);
	public native Buffer readFileSync(String path);
	public native void mkdirSync(String path, MkDirOpt opt);
	public native FSStats lstatSync(String path);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	public static class Buffer {

		public Buffer(String content, String string) {
		}

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

	@JsOverlay
	public final void mkdirs(String path) {
		MkDirOpt o = new MkDirOpt();
		o.recursive = true;
		mkdirSync(path, o);
	}
}
