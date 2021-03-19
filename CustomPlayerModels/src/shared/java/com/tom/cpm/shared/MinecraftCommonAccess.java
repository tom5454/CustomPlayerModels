package com.tom.cpm.shared;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;

public interface MinecraftCommonAccess {

	public static MinecraftCommonAccess get() {
		return MinecraftObjectHolder.commonObject;
	}

	public ModConfigFile getConfig();
}
