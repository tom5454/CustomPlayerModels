package com.tom.cpm.web.client;

import com.tom.cpm.web.client.java.io.FileNotFoundException;

import elemental2.dom.Blob;
import elemental2.dom.DomGlobal;
import elemental2.dom.File;
import elemental2.dom.HTMLLinkElement;
import elemental2.dom.Response;
import elemental2.dom.URL;
import elemental2.promise.Promise;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

public class FS {
	private static IFS impl;

	public static String[] list(String path) {
		if(impl == null)return new String[0];
		return impl.list(path);
	}

	public static boolean exists(String path) {
		if(impl == null)return false;
		return impl.exists(path);
	}

	public static boolean isDir(String path) {
		if(impl == null)return false;
		return impl.isDir(path);
	}

	public static void mkdir(String path) {
		if(impl == null)return;
		impl.mkdir(path);
	}

	public static String getContent(String path) throws FileNotFoundException {
		if(impl == null)throw new FileNotFoundException("File not found");
		return impl.getContent(path);
	}

	public static Promise<Response> getContentFuture(String path) {
		return new Promise<>((res, rej) -> {
			try {
				res.onInvoke(DomGlobal.fetch("data:application/octet-binary;base64," + getContent(path)));
			} catch (FileNotFoundException e) {
				rej.onInvoke(e);
			}
		});
	}

	public static boolean setContent(String path, String content) {
		if(impl == null)return false;
		return impl.setContent(path, content);
	}

	public static Promise<Object> mount(File file) {
		if(impl == null)return Promise.reject(null);
		return impl.mount(file);
	}

	public static void mount(String b64, String name) {
		if(impl == null)return;
		impl.mount(b64, name);
	}

	public static void deleteFile(String name) {
		if(impl == null)return;
		impl.deleteFile(name);
	}

	public static void setImpl(IFS impl) {
		FS.impl = impl;
	}

	public static boolean hasImpl() {
		return impl != null;
	}

	public static boolean needFileManager() {
		if(impl == null)return false;
		return impl.needFileManager();
	}

	public static interface IFS {
		String[] list(String path);
		boolean exists(String path);
		boolean isDir(String path);
		void mkdir(String path);
		String getContent(String path) throws FileNotFoundException;
		boolean setContent(String path, String content);
		void deleteFile(String path);
		Promise<Object> mount(File file);
		void mount(String b64, String name);
		boolean needFileManager();
	}

	public static void saveAs(Blob blob, String name) {
		HTMLLinkElementEx a = Js.uncheckedCast(DomGlobal.document.createElement("a"));
		String url = URL.createObjectURL(blob);
		a.href = url;
		a.download = name;
		DomGlobal.document.body.appendChild(a);
		a.click();
		DomGlobal.setTimeout(__ -> {
			DomGlobal.document.body.removeChild(a);
			URL.revokeObjectURL(url);
		}, 1);
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL)
	private static class HTMLLinkElementEx extends HTMLLinkElement {
		public String download;
		public native void click();
	}
}
