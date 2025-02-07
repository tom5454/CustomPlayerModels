package com.tom.cpm.retro;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.io.HTTPIO;
import com.tom.cpm.shared.skin.TextureType;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;
import com.tom.cpm.shared.util.Log;

public class GameProfileManager {
	private static final LoadingCache<String, ProfileLookup> usernameCache = CacheBuilder.newBuilder().build(CacheLoader.from(ProfileLookup::new));

	public static GameProfile getProfile(String name) {
		try {
			return usernameCache.get(name).get();
		} catch (ExecutionException e) {
			throw new RuntimeException("Failed to load player details: " + name, e);
		}
	}

	public static CompletableFuture<GameProfile> getProfileFuture(String name) {
		try {
			return usernameCache.get(name).getFuture();
		} catch (ExecutionException e) {
			throw new RuntimeException("Failed to load player details: " + name, e);
		}
	}

	private static void checkError(Map<String, Object> r) throws IOException {
		if (r == null)
			throw new IOException("Invalid data returned from the server");
		if (r.containsKey("error") || r.containsKey("errorMessage")) {
			String error = String.valueOf(r.get("error"));
			throw new IOException(error + ": " + String.valueOf(r.get("errorMessage")));
		}
	}

	private static class ProfileLookup {
		private GameProfile profile;
		private CompletableFuture<GameProfile> future;
		private Throwable exception;

		public ProfileLookup(String name) {
			UUID offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
			profile = new GameProfile(offline, name);
			if (name.isEmpty()) {
				future = CompletableFuture.completedFuture(profile);
				return;
			}
			future = CompletableFuture.supplyAsync(this::load, MinecraftClientAccess.get().getDefinitionLoader()::execute).
					handleAsync((r, ex) -> {
						if (ex != null) {
							exception = ex;
							return profile;
						}
						profile = r;
						return profile;
					}, MinecraftClientAccess.get()::executeOnGameThread);
		}

		@SuppressWarnings("unchecked")
		private GameProfile load() {
			String name = profile.getName();
			Log.debug("Fetching profile info: " + name);
			try {
				URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
				HttpURLConnection httpCon = HTTPIO.createUrlConnection(url, true);
				String resp = HTTPIO.getResponse(httpCon, url);
				Map<String, Object> r = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(resp, Object.class);
				checkError(r);
				String rawId = (String) r.get("id");
				Log.debug(name + ": " + rawId);
				url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + rawId);
				httpCon = HTTPIO.createUrlConnection(url, true);
				resp = HTTPIO.getResponse(httpCon, url);
				r = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(resp, Object.class);
				checkError(r);
				return new GameProfile(r);
			} catch (Exception e) {
				ErrorLog.addLog(LogLevel.WARNING, "Failed to load profile info for: " + name, e);
			}
			return profile;
		}

		public GameProfile get() {
			return profile;
		}

		public CompletableFuture<GameProfile> getFuture() {
			return future;
		}

		public Throwable getException() {
			return exception;
		}
	}

	public static String getTextureUrlSync(String username, TextureType type, String fallback) {
		try {
			return getProfileFuture(username).get().getTextureURLMap().getOrDefault(type, fallback);
		} catch (InterruptedException | ExecutionException e) {
			return fallback;
		}
	}

	public static void clear() {
		usernameCache.invalidateAll();
	}
}
