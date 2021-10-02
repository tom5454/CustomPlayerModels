package com.tom.cpm.shared.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import com.google.common.cache.LoadingCache;

import com.tom.cpl.util.HTTPMultipart;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.model.SkinType;

public class MojangSkinUploadAPI {
	private String auth;
	private UUID uuid;

	public MojangSkinUploadAPI(UUID uuid, String auth) {
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
		InputStream web = null;
		HttpURLConnection httpCon = null;
		try {
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setRequestProperty("Authorization", "Bearer " + auth);
			httpCon.setRequestProperty(HTTPMultipart.CONTENT_TYPE, req.getContentType());
			httpCon.setRequestProperty("Content-Length", Integer.toString(req.getLen()));
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			req.writeTo(httpCon.getOutputStream());
			web = httpCon.getInputStream();
			Log.info("[MojangSkinsAPI.uploadSkin]: Response " + httpCon.getResponseCode());
		} finally {
			if(httpCon != null)httpCon.disconnect();
			if(web != null)
				try {
					web.close();
				} catch (IOException e) {
				}
		}
	}

	/*public boolean checkAuth() {
		try {
			if(auth == null)throw new NullPointerException("Missing auth info");
			URL url = new URL("https://api.mojang.com/user/security/location");
			Log.info("[MojangSkinsAPI.checkAuth]: Authorizing mojang api");
			InputStream web = null;
			HttpURLConnection httpCon = null;
			try {
				httpCon = (HttpURLConnection) url.openConnection();
				httpCon.setRequestProperty("Authorization", "Bearer " + auth);
				web = httpCon.getInputStream();
				Log.info("[MojangSkinsAPI.checkAuth]: Response " + httpCon.getResponseCode());
				return true;
			} catch (IOException e) {
				Log.warn("Mojang API error", e);
			} finally {
				if(httpCon != null)httpCon.disconnect();
				if(web != null)
					try {
						web.close();
					} catch (IOException e) {
					}
			}
		} catch (Exception e) {
			Log.warn("[MojangSkinsAPI.checkAuth]: Failed to begin auth", e);
		}
		return false;
	}*/

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
}
