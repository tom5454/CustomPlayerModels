package com.tom.cpm.web.client.util;

import elemental2.dom.DomGlobal;
import elemental2.dom.FileReader;
import elemental2.promise.Promise;

public class CDNUtil {

	public static boolean isCDNLink(String url) {
		return url.startsWith(System.getProperty("cpm.cdnEndpoint"));
	}

	public static Promise<String> fetchFromCDN(String id) {
		if (id.length() != 32) {
			int i = id.lastIndexOf('/');
			int j = id.lastIndexOf('.');
			id = id.substring(i + 1, j);
		}
		if (id.length() != 32 && id.matches("[a-fA-F0-9]*"))return Promise.reject("Invalid cdn file id");
		return DomGlobal.fetch(System.getProperty("cpm.cdnEndpoint") + "/" + id).then(r -> r.blob()).then(b -> {
			return new Promise<String>((res, rej) -> {
				FileReader fr = new FileReader();
				fr.onload = e -> {
					String r = fr.result.asString();
					r = r.substring(r.indexOf(',') + 1);
					res.onInvoke(r);
					return null;
				};
				fr.onerror = e -> {
					rej.onInvoke(fr.error);
					return null;
				};
				fr.readAsDataURL(b);
			});
		});
	}
}
