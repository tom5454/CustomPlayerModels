package com.tom.cpm.shared.paste;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonParseException;

import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.io.HTTPIO;
import com.tom.cpm.shared.util.MojangAPI;

public class PasteClient {
	public static final String URL = "https://paste.tom5454.com";
	private MojangAPI mojang;
	private String url;
	private String session;
	private long loginTime = -1;
	private int maxSize = -1, maxPastes = -1;

	public PasteClient(MojangAPI mojang, String url) {
		this.mojang = mojang;
		this.url = url;
	}

	public void connect() throws IOException {
		URL url = new URL(this.url + "/api/connect?name=" + mojang.getName());
		HttpURLConnection httpCon = HTTPIO.createUrlConnection(url, false);
		String response = HTTPIO.getResponse(httpCon, url);
		Map<String, Object> r = parseResponse(response);
		session = (String) r.get("id");
		byte[] mojKey = Base64.getDecoder().decode((String) r.get("key"));
		byte[] mojangKey;
		try {
			mojangKey = digestData("tom5454-paste".getBytes(), session.getBytes(), mojKey);
		} catch (Exception e) {
			throw new LocalizedIOException("Unknown error", "error.paste.unknown", e);
		}
		mojang.joinServer(new BigInteger(mojangKey).toString(16));
		url = new URL(this.url + "/api/session");
		httpCon = createUrlConnection(url);
		response = HTTPIO.getResponse(httpCon, url);
		parseResponse(response);
		loginTime = System.currentTimeMillis();
	}

	@SuppressWarnings("unchecked")
	public List<Paste> listFiles() throws IOException {
		URL url = new URL(this.url + "/api/list");
		HttpURLConnection httpCon = createUrlConnection(url);
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
		return ret;
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

	public String uploadFile(String name, byte[] content) throws IOException {
		URL url = new URL(this.url + "/api/upload");
		HttpURLConnection httpCon = createUrlConnection(url);
		httpCon.setRequestProperty("File-Name", name);
		httpCon.setRequestProperty("Content-Length", Integer.toString(content.length));
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		httpCon.getOutputStream().write(content);
		String response = HTTPIO.getResponse(httpCon, url);
		Map<String, Object> r = parseResponse(response);
		return (String) r.get("id");
	}

	public void updateFile(String id, byte[] content) throws IOException {
		URL url = new URL(this.url + "/api/update?file=" + id);
		HttpURLConnection httpCon = createUrlConnection(url);
		httpCon.setRequestProperty("Content-Length", Integer.toString(content.length));
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		httpCon.getOutputStream().write(content);
		parseResponse(HTTPIO.getResponse(httpCon, url));
	}


	public void deleteFile(String id) throws IOException {
		URL url = new URL(this.url + "/api/delete?file=" + id);
		HttpURLConnection httpCon = createUrlConnection(url);
		parseResponse(HTTPIO.getResponse(httpCon, url));
	}

	private HttpURLConnection createUrlConnection(URL url) throws IOException {
		HttpURLConnection httpCon = HTTPIO.createUrlConnection(url, false);
		httpCon.setRequestProperty("Session", session);
		return httpCon;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseResponse(String response) throws IOException {
		try {
			Map<String, Object> r = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(response, Object.class);
			if(r.containsKey("error")) {
				if("error.paste.invalidSession".equals(r.get("errorMessage")))
					loginTime = -1;
				throw new LocalizedIOException(String.valueOf(r.get("error")), String.valueOf(r.get("errorMessage")));
			}
			return r;
		} catch (ClassCastException | JsonParseException e) {
			throw new LocalizedIOException("Result parse error", "error.paste.parseError", e);
		}
	}

	public static class LocalizedIOException extends IOException {
		private static final long serialVersionUID = -6332369839511402034L;
		private String loc;

		public LocalizedIOException(String msg, String loc) {
			super(msg);
			this.loc = loc;
		}

		public LocalizedIOException(String msg, String loc, Throwable thr) {
			super(msg, thr);
			this.loc = loc;
		}

		public String getLoc() {
			return loc;
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
}
