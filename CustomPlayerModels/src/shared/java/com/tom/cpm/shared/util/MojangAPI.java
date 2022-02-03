package com.tom.cpm.shared.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.cache.LoadingCache;
import com.google.gson.JsonParseException;

import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.HTTPMultipart;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.LocalizedIOException;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.io.HTTPIO;
import com.tom.cpm.shared.model.SkinType;

public class MojangAPI {
	private String name;
	private String auth;
	private UUID uuid;

	public MojangAPI(String name, UUID uuid, String auth) {
		this.name = name;
		this.uuid = uuid;
		this.auth = auth;
	}

	public void uploadSkin(SkinType skinType, Image skin) throws IOException {
		if(skinType == null || skinType == SkinType.UNKNOWN)throw new IOException("Invalid skin type");
		if(uuid == null || auth == null)throw new IOException("Missing auth info");

		URL url = new URL("https://api.minecraftservices.com/minecraft/profile/skins");

		HTTPMultipart req = new HTTPMultipart();
		req.addString("variant", skinType.getApiName().toLowerCase());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		skin.storeTo(baos);
		req.addBinary("file", "skin.png", "image/png", baos.toByteArray());
		req.encode();

		Log.info("[MojangSkinsAPI.uploadSkin]: Uploading skin");
		HttpURLConnection httpCon = HTTPIO.createUrlConnection(url, true);
		httpCon.setRequestProperty("Authorization", "Bearer " + auth);
		httpCon.setRequestProperty(HTTPMultipart.CONTENT_TYPE, req.getContentType());
		httpCon.setRequestProperty("Content-Length", Integer.toString(req.getLen()));
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		req.writeTo(httpCon.getOutputStream());
		String response = HTTPIO.getResponse(httpCon, url);
		parseError(response);
		Log.info("[MojangSkinsAPI.uploadSkin]: Response " + httpCon.getResponseCode() + ": " + response);
	}

	public void joinServer(String serverId) throws IOException {
		if(uuid == null || auth == null)throw new IOException("Missing auth info");
		String response;
		try {
			URL url = new URL("https://sessionserver.mojang.com/session/minecraft/join");
			HttpURLConnection httpCon = HTTPIO.createUrlConnection(url, true);
			Map<String, String> data = new HashMap<>();
			data.put("accessToken", auth);
			data.put("selectedProfile", fromUUID(uuid));
			data.put("serverId", serverId);
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			httpCon.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			byte[] d = MinecraftObjectHolder.gson.toJson(data).getBytes("UTF-8");
			httpCon.setRequestProperty("Content-Length", "" + d.length);
			httpCon.getOutputStream().write(d);
			response = HTTPIO.getResponse(httpCon, url);
			Log.info("[MojangSkinsAPI.joinServer]: Response " + httpCon.getResponseCode() + ": " + response);
		} catch (IOException | JsonParseException e) {
			throw new LocalizedIOException("Cannot contact authentication server", new FormatText("disconnect.loginFailedInfo.serversUnavailable"), e);
		}
		parseError(response);
	}

	@SuppressWarnings("unchecked")
	private static void parseError(String response) throws IOException {
		try {
			Map<String, Object> r = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(response, Object.class);
			if(r != null && r.containsKey("error")) {
				String error = String.valueOf(r.get("error"));
				if(error.equals("ForbiddenOperationException")) {
					throw new LocalizedIOException("ForbiddenOperationException", new FormatText("disconnect.loginFailedInfo.invalidSession"));
				} else if(error.equals("InsufficientPrivilegesException")) {
					throw new LocalizedIOException("InsufficientPrivilegesException", new FormatText("disconnect.loginFailedInfo.insufficientPrivileges"));
				}
				throw new IOException(error + ": " + String.valueOf(r.get("errorMessage")));
			}
		} catch (ClassCastException | JsonParseException e) {
		}
	}

	public static String fromUUID(final UUID value) {
		return value.toString().replace("-", "");
	}

	public static void clearYggdrasilCache(Object yss) {
		try {
			for(Field f : yss.getClass().getDeclaredFields()) {
				if(f.getType() == LoadingCache.class) {
					f.setAccessible(true);
					LoadingCache<?, ?> cache = (LoadingCache<?, ?>) f.get(yss);
					cache.invalidateAll();
					return;
				}
			}
			throw new NoSuchFieldError("Couldn't find cache in " + yss);
		} catch (Throwable e) {
			Log.warn("Failed to clear skin cache", e);
		}
	}

	public String getName() {
		return name;
	}
}
