package com.tom.cpm.shared.paste;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.LocalizedIOException;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.util.MojangAPI;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.java.Java;
import com.tom.ugwt.client.JsArrayE;

import elemental2.core.Global;
import elemental2.core.Uint8Array;
import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;
import elemental2.promise.Promise;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class PasteClient {
	public static final boolean CAN_OPEN_BROWSER = false;
	public static final String PASTE_TOKEN_KEY = "pastetoken";
	public static final String URL = "https://paste.tom5454.com";
	public static final String URL_CF = "https://cf-paste.tom5454.com";
	private String url;
	private String session;
	private long loginTime = -1;
	private int maxSize = -1, maxPastes = -1;

	public PasteClient(MojangAPI mojang, String url, String fallback) {
		this.url = url;
	}

	private static <T> CompletableFuture<T> error() {
		CompletableFuture<T> cf = new CompletableFuture<>();
		cf.completeExceptionally(new LocalizedIOException("Unknown error", new FormatText("error.paste.unknown")));
		return cf;
	}

	@SuppressWarnings("unchecked")
	private <T> CompletableFuture<T> fetch(String url, RequestInit req, Function<Promise<JsPropertyMap<Object>>, Promise<T>> process) {
		CompletableFuture<T> cf = new CompletableFuture<>();
		Java.promiseToCf(process.apply(DomGlobal.fetch(url, req).then(r -> {
			if(!r.ok)return r.text().then(Promise::reject);
			return r.json().then(Js::cast);
		})).catch_(r -> {
			if (r instanceof String) {
				JsPropertyMap<String> map = (JsPropertyMap<String>) Global.JSON.parse((String) r);
				if(map.has("error")) {
					if("error.paste.invalidSession".equals(map.get("errorMessage")))
						loginTime = -1;
					return Promise.reject(new LocalizedIOException(String.valueOf(map.get("error")), new FormatText(String.valueOf(map.get("errorMessage")))));
				}
				return Promise.reject(map);
			}
			return Promise.reject(r);
		}), cf);
		return cf;
	}

	private <T> CompletableFuture<T> fetch(String url, Function<Promise<JsPropertyMap<Object>>, Promise<T>> process) {
		RequestInit req = RequestInit.create();
		req.setHeaders(RequestInit.GetHeadersUnionType.of(JsPropertyMap.of("Session", session)));
		return fetch(url, req, process);
	}

	private CompletableFuture<Void> getPasteToken(CompletableFuture<Void>[] cf) {
		RequestInit req = RequestInit.create();
		String appid = WebMC.getInstance().getAppID();
		req.setHeaders(RequestInit.GetHeadersUnionType.of(JsPropertyMap.of("AppID", appid)));
		return fetch(this.url + "/api/app_login", req, f -> f.then(j -> {
			String id = (String) j.get("id");
			WebMC.getInstance().getGui().openURL(this.url + "/auth.html?id=" + id);
			return new Promise<>((res, rej) -> {
				int[] cnt = new int[] {0};
				double[] t = new double[1];
				t[0] = DomGlobal.setInterval(__ -> {
					if(cnt[0]++ > 30 || cf[0].isDone()) {
						DomGlobal.clearInterval(t[0]);
						rej.onInvoke(new IOException("Auth Timeout"));
					} else {
						RequestInit req1 = RequestInit.create();
						req1.setHeaders(RequestInit.GetHeadersUnionType.of(JsPropertyMap.of("AppID", appid, "Session", id)));
						fetch(this.url + "/api/app_check", req1, f1 -> f1.then(j1 -> {
							if(j1.has("id")) {
								DomGlobal.clearInterval(t[0]);
								ModConfig.getCommonConfig().setString(PASTE_TOKEN_KEY, (String) j1.get("id"));
								ModConfig.getCommonConfig().save();
								res.onInvoke((Void) null);
							}
							return null;
						})).exceptionally(ex -> {
							DomGlobal.clearInterval(t[0]);
							rej.onInvoke(ex);
							return null;
						});
					}
				}, 5000);
			});
		}));
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> connect() {
		CompletableFuture<Void>[] cf = new CompletableFuture[1];
		cf[0] = connect0(true, cf);
		return cf[0];
	}

	private CompletableFuture<Void> connect0(boolean retry, CompletableFuture<Void>[] cfIn) {
		String token = ModConfig.getCommonConfig().getString(PASTE_TOKEN_KEY, null);
		if(token == null) {
			if(!retry) {
				CompletableFuture<Void> cf = new CompletableFuture<>();
				cf.completeExceptionally(new LocalizedIOException("Invalid Session", new FormatText("error.paste.invalidSession")));
				return cf;
			}
			return getPasteToken(cfIn).thenCompose(v -> connect0(false, cfIn));
		}
		RequestInit req = RequestInit.create();
		req.setHeaders(RequestInit.GetHeadersUnionType.of(JsPropertyMap.of("Session", token)));
		return fetch(this.url + "/api/app_token_req", req, f -> f.then(j -> {
			session = (String) j.get("id");
			loginTime = System.currentTimeMillis();
			return Promise.resolve((Throwable) null);
		})).exceptionally(ex -> {
			if(retry)return new RetryEx();
			return ex;
		}).thenCompose(v -> {
			if(v == null)return CompletableFuture.completedFuture(null);
			if(v instanceof RetryEx) {
				return getPasteToken(cfIn).thenCompose(__ -> connect0(false, cfIn));
			}
			CompletableFuture<Void> cf = new CompletableFuture<>();
			cf.completeExceptionally(v);
			return cf;
		});
	}

	private static class RetryEx extends Throwable {
		private static final long serialVersionUID = -177279308716339177L;
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<List<Paste>> listFiles() {
		return fetch(this.url + "/api/list", o -> o.then(r -> {
			JsArrayE<Paste> pastes = (JsArrayE<Paste>) r.get("files");
			maxSize = Js.asInt(r.get("maxSize"));
			maxPastes = Js.asInt(r.get("maxFiles"));
			return Promise.resolve(pastes.asList());
		}));
	}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
	public static class Paste {
		public String id, name;
		public long time;
	}

	public CompletableFuture<String> uploadFile(String name, byte[] content) {
		RequestInit req = RequestInit.create();
		req.setHeaders(RequestInit.GetHeadersUnionType.of(JsPropertyMap.of("Session", session, "File-Name", name)));
		req.setMethod("POST");
		req.setBody(new Uint8Array(Uint8Array.ConstructorLengthUnionType.of(content)).buffer);
		return fetch(this.url + "/api/upload", req, p -> p.then(j -> Promise.resolve((String) j.get("id"))));
	}

	public CompletableFuture<Void> updateFile(String id, byte[] content) {
		RequestInit req = RequestInit.create();
		req.setHeaders(RequestInit.GetHeadersUnionType.of(JsPropertyMap.of("Session", session, "Content-Length", Integer.toString(content.length))));
		req.setMethod("POST");
		req.setBody(new Uint8Array(Uint8Array.ConstructorLengthUnionType.of(content)).buffer);
		return fetch(this.url + "/api/update?file=" + id, req, p -> p.then(j -> Promise.resolve((Void) null)));
	}

	public CompletableFuture<Void> deleteFile(String id) {
		return fetch(this.url + "/api/delete?file=" + id, p -> p.then(j -> Promise.resolve((Void) null)));
	}

	public CompletableFuture<String> createBrowserLoginURL() {
		return error();
	}

	public CompletableFuture<Void> logout() {
		return fetch(this.url + "/api/logout_app", p -> p.then(j -> {
			session = null;
			loginTime = -1;
			ModConfig.getCommonConfig().clearValue(PASTE_TOKEN_KEY);
			ModConfig.getCommonConfig().save();
			return Promise.resolve((Void) null);
		}));
	}

	public boolean isConnected() {
		return loginTime != -1 && System.currentTimeMillis() - loginTime < 15*60*1000;
	}

	public int getMaxPastes() {
		return maxPastes;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public String getUrl() {
		return url;
	}
}
