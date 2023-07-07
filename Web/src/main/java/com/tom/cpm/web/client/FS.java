package com.tom.cpm.web.client;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpm.web.client.java.io.FileNotFoundException;

import elemental2.dom.DomGlobal;
import elemental2.dom.Response;
import elemental2.promise.Promise;

public class FS {
	private static IFS impl;

	public static Promise<String> getContent(String path) {
		if(impl == null)return Promise.reject(new FileNotFoundException());
		return impl.getContent(path);
	}

	public static String getContentSync(String path) throws FileNotFoundException {
		if(impl == null)throw new FileNotFoundException();
		return impl.getContentSync(path);
	}

	public static Promise<Response> getContentFuture(String path) {
		if(impl == null)return Promise.reject(new FileNotFoundException());
		return impl.getContentBinary(path);
	}

	public static Promise<Void> setContent(String path, String content) {
		if(impl == null)return Promise.reject(new FileNotFoundException());
		return impl.setContent(path, content);
	}

	public static Promise<File> mount(elemental2.dom.File file) {
		if(impl == null)return Promise.reject(null);
		return impl.mount(file);
	}

	public static void mount(String b64, String name) {
		if(impl == null)return;
		impl.mount(b64, name);
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

	public static String getWorkDir() {
		if(impl == null)return "";
		return impl.getWorkDir();
	}

	public static CompletableFuture<File> openFileChooser(FileChooserPopup fcp) {
		if(impl == null)return null;
		return impl.openFileChooser(fcp);
	}

	public static interface IFS {
		String getContentSync(String path) throws FileNotFoundException;

		default Promise<String> getContent(String path) {
			return new Promise<>((res, rej) -> {
				try {
					res.onInvoke(getContentSync(path));
				} catch (FileNotFoundException e) {
					rej.onInvoke(e);
				}
			});
		}

		default Promise<Response> getContentBinary(String path) {
			return getContent(path).then(v -> DomGlobal.fetch("data:application/octet-binary;base64," + v));
		}

		Promise<Void> setContent(String path, String content);
		Promise<File> mount(elemental2.dom.File file);
		void mount(String b64, String name);
		boolean needFileManager();

		IFile getImpl(IFile parent, String path);
		String getWorkDir();
		CompletableFuture<File> openFileChooser(FileChooserPopup fcp);
	}

	public static IFile getImpl(IFile file, String name) {
		if(impl == null)return null;
		return impl.getImpl(file, name);
	}

	public static IFile getImpl(String name) {
		if(impl == null)return null;
		return impl.getImpl(null, name);
	}

	public static IFS getImpl() {
		return impl;
	}
}
