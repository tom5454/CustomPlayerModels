package com.tom.cpm.blockbench.proxy;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_fs_$$")
public class FileSystem {
	public static native boolean existsSync(String path);
}
