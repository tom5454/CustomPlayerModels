package com.tom.cpm.shared.util;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.util.Image;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.util.ImageIO;

import elemental2.dom.DomGlobal;
import elemental2.dom.FileReader;
import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.RejectCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;

public class MdResourceIO {
	public static CompletableFuture<Image> loadImage0(String url, boolean offline) {
		return MdResourceLoader.fetch(url, offline).thenCompose(ImageIO::loadImage);
	}

	public static CompletableFuture<byte[]> fetch0(String url) {
		String file = url.substring(url.lastIndexOf('/') + 1);
		if(url.startsWith("https://github.com/tom5454/CustomPlayerModels/wiki/")) {
			return fetch("p:" + file);
		} else if(url.startsWith("https://github.com/tom5454/CustomPlayerModels/raw/master/screenshots/")) {
			return fetch("i:" + file);
		} else if(url.startsWith("changelog")) {
			return fetch("c:c.md");
		}
		CompletableFuture<byte[]> cf = new CompletableFuture<>();
		cf.completeExceptionally(new IOException("Web editor cannot load websites"));
		return cf;
	}

	private static CompletableFuture<byte[]> fetch(String path) {
		CompletableFuture<byte[]> cf = new CompletableFuture<>();
		Java.promiseToCf(DomGlobal.fetch(System.getProperty("cpm.webApiEndpoint") + "/wiki?v=" + path).then(b -> b.blob()).
				then(blob -> new Promise<>((ResolveCallbackFn<String> res, RejectCallbackFn rej) -> {
					FileReader reader = new FileReader();
					reader.onloadend = __ -> {
						res.onInvoke(reader.result.asString());
						return null;
					};
					reader.onerror = __ -> {
						rej.onInvoke(null);
						return null;
					};
					reader.readAsDataURL(blob);
				})).then(dataUrl -> {
					String b64 = dataUrl.substring(dataUrl.indexOf(',') + 1);
					return Promise.resolve(Base64.getDecoder().decode(b64));
				}), cf);
		return cf;
	}
}
