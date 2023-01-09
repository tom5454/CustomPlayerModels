package com.tom.cpm.blockbench.proxy.electron;

import elemental2.promise.Promise;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "")
public class ElectronDialog {
	public native Promise<OpenDialogResult> showOpenDialog(DialogProperties pr);
	public native Promise<SaveDialogResult> showSaveDialog(DialogProperties pr);

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class DialogProperties {
		public String[] properties;
		public String title, defaultPath, buttonLabel;
		public FileFilterJS[] filters;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class OpenDialogResult {
		public boolean canceled;
		public String[] filePaths;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class SaveDialogResult {
		public boolean canceled;
		public String filePath;
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class FileFilterJS {
		public String name;
		public String[] extensions;

		@JsOverlay
		public static FileFilterJS make(String name, String... extentions) {
			FileFilterJS ff = new FileFilterJS();
			ff.name = name;
			ff.extensions = extentions;
			return ff;
		}
	}
}
