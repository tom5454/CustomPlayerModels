package com.tom.cpmpvc;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.tom.cpm.api.IClientAPI.MessageSender;
import com.tom.cpm.shared.MinecraftClientAccess;
import su.plo.voice.api.util.AudioUtil;

public class CPMPVC {
	public static final String MOD_ID = "cpmpvc";
	public static final Logger LOGGER = LogManager.getLogger("CPM-PV Compat");
	private static final LoadingCache<UUID, Float> voiceLevelsCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build(CacheLoader.from(() -> 0f));
	public static MessageSender mutedSender;

	public static float get(UUID uuid) {
		try {
			return voiceLevelsCache.get(uuid);
		} catch (Exception e) {
			return 0;
		}
	}

	public static void handle(short[] data) {
		handle(MinecraftClientAccess.get().getCurrentClientPlayer().getUUID(), data);
	}

	public static void handle(UUID uuid, short[] data) {
		voiceLevelsCache.put(uuid, data == null ? 0f : calcVoiceLevel(data));
	}

	private static float calcVoiceLevel(short[] data) {
		return (float) AudioUtil.audioLevelToDoubleRange(AudioUtil.calculateHighestAudioLevel(data));
	}

	public static boolean isMuted(UUID uuid) {
		return CPMAddon.INSTANCE.isMuted(uuid);
	}
}
