package com.tom.cpm.shared;

import com.tom.cpm.shared.config.ConfigEntry.ModConfig;

public interface MinecraftCommonAccess {

	public static MinecraftCommonAccess get() {
		return MinecraftObjectHolder.commonObject;
	}

	public ModConfig getConfig();
}
