package com.tom.cpm.shared.config;

import java.util.HashMap;
import java.util.Map;

import com.tom.cpl.config.ConfigEntry;

public enum BuiltInSafetyProfiles {
	OFF(
			ConfigKeys.ENABLE_MODEL_LOADING, true,
			ConfigKeys.ENABLE_ANIMATED_TEXTURES, true,
			ConfigKeys.MAX_TEX_SHEET_SIZE, 8192,
			ConfigKeys.MAX_LINK_SIZE, 256*1024,
			ConfigKeys.MAX_CUBE_COUNT, Integer.MAX_VALUE
			),
	LOW(
			ConfigKeys.ENABLE_MODEL_LOADING, true,
			ConfigKeys.ENABLE_ANIMATED_TEXTURES, true,
			ConfigKeys.MAX_TEX_SHEET_SIZE, 512,
			ConfigKeys.MAX_LINK_SIZE, 1024,
			ConfigKeys.MAX_CUBE_COUNT, 1024
			),
	MEDIUM(
			ConfigKeys.ENABLE_MODEL_LOADING, true,
			ConfigKeys.ENABLE_ANIMATED_TEXTURES, false,
			ConfigKeys.MAX_TEX_SHEET_SIZE, 256,
			ConfigKeys.MAX_LINK_SIZE, 100,
			ConfigKeys.MAX_CUBE_COUNT, 256
			),
	HIGH(
			ConfigKeys.ENABLE_MODEL_LOADING, false,
			ConfigKeys.ENABLE_ANIMATED_TEXTURES, false,
			ConfigKeys.MAX_TEX_SHEET_SIZE, 0,
			ConfigKeys.MAX_LINK_SIZE, 0,
			ConfigKeys.MAX_CUBE_COUNT, 0
			),
	CUSTOM()
	;
	private final Map<String, Object> str2data;
	private final Map<PlayerSpecificConfigKey<?>, Object> key2data;
	private BuiltInSafetyProfiles(Object... dataIn) {
		str2data = new HashMap<>();
		key2data = new HashMap<>();
		if(dataIn.length > 0) {
			for(int i = 0;i<dataIn.length;i += 2) {
				PlayerSpecificConfigKey<?> key = (PlayerSpecificConfigKey<?>) dataIn[i];
				Object v = dataIn[i + 1];
				str2data.put(key.getName(), v);
				key2data.put(key, v);
			}
			for(PlayerSpecificConfigKey<?> k : ConfigKeys.SAFETY_KEYS) {
				if(!str2data.containsKey(k.getName()))throw new RuntimeException("Missing value for '" + k.getName() + "' in '" + this + "'");
			}
		}
	}

	public static final BuiltInSafetyProfiles[] VALUES = values();

	public static BuiltInSafetyProfiles get(String name) {
		for (int i = 0; i < VALUES.length; i++) {
			BuiltInSafetyProfiles v = VALUES[i];
			if(v.name().equalsIgnoreCase(name))return v;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <V> V getValue(String name) {
		return (V) str2data.get(name);
	}

	public void copyTo(ConfigEntry ce) {
		key2data.forEach((k, v) -> copyTo$setValue(ce, k, v));
	}

	@SuppressWarnings("unchecked")
	private static <T> void copyTo$setValue(ConfigEntry ce, PlayerSpecificConfigKey<T> key, Object value) {
		key.setValue(ce, (T) value);
	}
}
