package com.tom.cpmsvcc;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.tom.cpm.api.ICPMPlugin;

public class CPMSVCC {
	public static final String MOD_ID = "cpmsvcc";
	public static final Logger LOGGER = LogManager.getLogger("CPM-SVC Compat");
	private static final LoadingCache<UUID, Float> voiceLevelsCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build(CacheLoader.from(() -> 0f));
	private static Supplier<UUID> localGetter;

	public static float get(UUID uuid) {
		try {
			return voiceLevelsCache.get(uuid);
		} catch (Exception e) {
			return 0;
		}
	}

	public static ICPMPlugin make(String platform) {
		try {
			return (ICPMPlugin) Class.forName("com.tom.cpmsvcc.platform." + platform + ".CPMSVCPlugin").getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to construct platform plugin: " + platform, e);
		}
	}

	public static void setLocal(Supplier<UUID> localGetter) {
		CPMSVCC.localGetter = localGetter;
	}

	public static void handle(short[] data) {
		handle(localGetter.get(), data);
	}

	public static void handle(UUID uuid, short[] data) {
		voiceLevelsCache.put(uuid, calcVoiceLevel(data));
	}

	private static float calcVoiceLevel(short[] data) {
		return (float) dbToPerc(getHighestAudioLevel(data));
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
