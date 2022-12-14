package com.tom.cpmsvcc;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.api.IClientAPI.MessageSender;
import com.tom.cpm.shared.MinecraftClientAccess;

public class CPMSVCC {
	public static final String MOD_ID = "cpmsvcc";
	public static final Logger LOGGER = LogManager.getLogger("CPM-SVC Compat");
	private static final LoadingCache<UUID, Float> voiceLevelsCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build(CacheLoader.from(() -> 0f));
	public static final Set<UUID> muted = new HashSet<>();
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
		return (float) dbToPerc(getHighestAudioLevel(data));
	}

	public static void setMuted(boolean muted) {
		if(mutedSender != null) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setBoolean("muted", muted);
			mutedSender.sendMessage(tag);
		}
		if(muted)CPMSVCC.muted.add(MinecraftClientAccess.get().getCurrentClientPlayer().getUUID());
		else CPMSVCC.muted.remove(MinecraftClientAccess.get().getCurrentClientPlayer().getUUID());
	}

	public static boolean isMuted(UUID uuid) {
		return muted.contains(uuid);
	}

	/**
	 * Calculates the audio level of a signal with specific samples.
	 *
	 * @param samples the samples of the signal to calculate the audio level of
	 * @param offset  the offset in samples in which the samples start
	 * @param length  the length in bytes of the signal in samples starting at offset
	 * @return the audio level of the specified signal in db
	 */
	private static double calculateAudioLevel(short[] samples, int offset, int length) {
		double rms = 0D; // root mean square (RMS) amplitude

		for (int i = offset; i < length; i++) {
			double sample = (double) samples[i] / (double) Short.MAX_VALUE;
			rms += sample * sample;
		}

		int sampleCount = length / 2;

		rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

		double db;

		if (rms > 0D) {
			db = Math.min(Math.max(20D * Math.log10(rms), -127D), 0D);
		} else {
			db = -127D;
		}

		return db;
	}

	/**
	 * Calculates the highest audio level in packs of 100
	 *
	 * @param samples the audio samples
	 * @return the audio level in db
	 */
	private static double getHighestAudioLevel(short[] samples) {
		double highest = -127D;
		for (int i = 0; i < samples.length; i += 100) {
			double level = calculateAudioLevel(samples, i, Math.min(i + 100, samples.length));
			if (level > highest) {
				highest = level;
			}
		}
		return highest;
	}

	private static double dbToPerc(double db) {
		return (db + 127D) / 127D;
	}
}
