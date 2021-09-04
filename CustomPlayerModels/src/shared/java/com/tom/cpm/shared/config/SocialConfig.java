package com.tom.cpm.shared.config;

import com.tom.cpl.config.ConfigEntry;

public class SocialConfig {

	public static boolean isFriend(String uuid) {
		ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.FRIEND_LIST);
		return ce.hasEntry(uuid);
	}

	public static void addFriend(String uuid, String name) {
		ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.FRIEND_LIST);
		ce.getEntry(uuid).setString(ConfigKeys.NAME, name);
	}

	public static void blockPlayer(String uuid, String name) {
		ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.BLOCKED_LIST);
		ce.getEntry(uuid).setString(ConfigKeys.NAME, name);
	}

	public static boolean isBlocked(String uuid) {
		ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.BLOCKED_LIST);
		return ce.hasEntry(uuid);
	}

	public static BuiltInSafetyProfiles getProfile(String[] spf) {
		BuiltInSafetyProfiles profile = BuiltInSafetyProfiles.get(spf[0]);
		if(profile == null && spf.length == 2)profile = BuiltInSafetyProfiles.CUSTOM;
		if(profile == null)profile = BuiltInSafetyProfiles.MEDIUM;
		if(profile == BuiltInSafetyProfiles.CUSTOM && spf.length == 1)profile = BuiltInSafetyProfiles.MEDIUM;
		return profile;
	}

	public static void removeFriend(String uuid) {
		ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.FRIEND_LIST);
		ce.clearValue(uuid);
	}

	public static void removeBlock(String uuid) {
		ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.BLOCKED_LIST);
		ce.clearValue(uuid);
	}
}
