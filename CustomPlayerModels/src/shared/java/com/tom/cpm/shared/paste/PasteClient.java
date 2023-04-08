package com.tom.cpm.shared.paste;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonParseException;

import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.LocalizedIOException;
import com.tom.cpl.util.ThrowingConsumer;
import com.tom.cpl.util.Util;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.HTTPIO;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.MojangAPI;

public class PasteClient {
	public static final String URL = "https://paste.tom5454.com";
	public static final String URL_CF = "https://cf-paste.tom5454.com";
	private MojangAPI mojang;
	private String url, fallback;
	private String session;
	private long loginTime = -1;
	private int maxSize = -1, maxPastes = -1;

	public PasteClient(MojangAPI mojang, String url, String fallback) {
		this.mojang = mojang;
		this.url = url;
		this.fallback = fallback;
	}

	private static <T> CompletableFuture<T> run(ThrowingConsumer<CompletableFuture<T>, IOException> task) {
		CompletableFuture<T> cf = new CompletableFuture<>();
		ModelDefinitionLoader.THREAD_POOL.execute(() -> {
			try {
				task.accept(cf);
			} catch (IOException e) {
				cf.completeExceptionally(e);
			}
		});
		return cf;
	}

	private static void disconnectOnError(CompletableFuture<?> cf, HttpURLConnection httpCon) {
		cf.exceptionally(x -> {
			Util.closeQuietly(httpCon::disconnect);
			return null;
		});
	}

	public CompletableFuture<Void> connect() {
		return run(cf -> {
			URL url = new URL(this.url + "/api/connect?name=" + mojang.getName());
			HttpURLConnection httpCon = HTTPIO.createUrlConnection(url, false);
			disconnectOnError(cf, httpCon);
			String response;
			try {
				response = HTTPIO.getResponse(httpCon, url);
				Log.info("[Paste API]: Connected to paste site");
			} catch (SocketTimeoutException | ConnectException e) {
				Log.info("[Paste API]: Paste site timeout, trying fallback");
				url = new URL(this.fallback + "/api/connect?name=" + mojang.getName());
				httpCon = HTTPIO.createUrlConnection(url, false);
				disconnectOnError(cf, httpCon);
				response = HTTPIO.getResponse(httpCon, url);
				this.url = this.fallback;
				Log.info("[Paste API]: Connection success using fallback");
			}
			Map<String, Object> r = parseResponse(response);
			session = (String) r.get("id");
			byte[] mojKey = Base64.getDecoder().decode((String) r.get("key"));
			byte[] mojangKey;
			try {
				mojangKey = digestData("tom5454-paste".getBytes(), session.getBytes(), mojKey);
			} catch (Exception e) {
				throw new LocalizedIOException("Unknown error", new FormatText("error.paste.unknown"), e);
			}
			try {
				mojang.joinServer(new BigInteger(mojangKey).toString(16));
			} catch (LocalizedIOException e) {
				throw new LocalizedIOException(e.getMessage(), new FormatText("error.paste.authFail", e.getLocalizedText()), e.getCause());
			}
			url = new URL(this.url + "/api/session");
			httpCon = createUrlConnection(url, cf);
			response = HTTPIO.getResponse(httpCon, url);
			parseResponse(response);
			loginTime = System.currentTimeMillis();
			Log.info("[Paste API]: Connection success");
			cf.complete(null);
		});
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<List<Paste>> listFiles() {
		return run(cf -> {
			URL url = new URL(this.url + "/api/list");
			HttpURLConnection httpCon = createUrlConnection(url, cf);
			String response = HTTPIO.getResponse(httpCon, url);
			Map<String, Object> r = parseResponse(response);
			List<Map<String, Object>> files = (List<Map<String, Object>>) r.get("files");
			List<Paste> ret = new ArrayList<>();
			files.forEach(m -> {
				String id = (String) m.get("id");
				String name = (String) m.get("name");
				long time = Long.parseLong((String) m.get("time"));
				ret.add(new Paste(id, name, time));
			});
			maxSize = ((Number) r.get("maxSize")).intValue();
			maxPastes = ((Number) r.get("maxFiles")).intValue();
			cf.complete(ret);
		});
	}

	public static class Paste {
		public final String id, name;
		public final long time;

		public Paste(String id, String name, long time) {
			this.id = id;
			this.name = name;
			this.time = time;
		}
	}

	public CompletableFuture<String> uploadFile(String name, byte[] content) {
		return run(cf -> {
			URL url = new URL(this.url + "/api/upload");
			HttpURLConnection httpCon = createUrlConnection(url, cf);
			httpCon.setRequestProperty("File-Name", name);
			httpCon.setRequestProperty("Content-Length", Integer.toString(content.length));
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			httpCon.getOutputStream().write(content);
			String response = HTTPIO.getResponse(httpCon, url);
			Map<String, Object> r = parseResponse(response);
			cf.complete((String) r.get("id"));
		});
	}

	public CompletableFuture<Void> updateFile(String id, byte[] content) {
		return run(cf -> {
			URL url = new URL(this.url + "/api/update?file=" + id);
			HttpURLConnection httpCon = createUrlConnection(url, cf);
			httpCon.setRequestProperty("Content-Length", Integer.toString(content.length));
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			httpCon.getOutputStream().write(content);
			parseResponse(HTTPIO.getResponse(httpCon, url));
			cf.complete(null);
		});
	}

	public CompletableFuture<Void> deleteFile(String id) {
		return run(cf -> {
			URL url = new URL(this.url + "/api/delete?file=" + id);
			HttpURLConnection httpCon = createUrlConnection(url, cf);
			parseResponse(HTTPIO.getResponse(httpCon, url));
			cf.complete(null);
		});
	}

	public CompletableFuture<String> createBrowserLoginURL() {
		return run(cf -> {
			URL url = new URL(this.url + "/api/browser_login");
			HttpURLConnection httpCon = createUrlConnection(url, cf);
			String response = HTTPIO.getResponse(httpCon, url);
			Map<String, Object> r = parseResponse(response);
			cf.complete((String) r.get("id"));
		});
	}

	private HttpURLConnection createUrlConnection(URL url, CompletableFuture<?> cf) throws IOException {
		HttpURLConnection httpCon = HTTPIO.createUrlConnection(url, false);
		httpCon.setRequestProperty("Session", session);
		disconnectOnError(cf, httpCon);
		return httpCon;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseResponse(String response) throws IOException {
		try {
			Map<String, Object> r = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(response, Object.class);
			if(r.containsKey("error")) {
				if("error.paste.invalidSession".equals(r.get("errorMessage")))
					loginTime = -1;
				throw new LocalizedIOException(String.valueOf(r.get("error")), new FormatText(String.valueOf(r.get("errorMessage"))));
			}
			return r;
		} catch (ClassCastException | JsonParseException e) {
			throw new LocalizedIOException("Result parse error", new FormatText("error.paste.parseError"), e);
		}
	}

	private static byte[] digestData(byte[]... p_244731_0_) throws Exception {
		MessageDigest messagedigest = MessageDigest.getInstance("SHA-1");

		for(byte[] abyte : p_244731_0_) {
			messagedigest.update(abyte);
		}

		return messagedigest.digest();
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
