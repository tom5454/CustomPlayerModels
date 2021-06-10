package com.tom.cpm.shared.config;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftServerAccess;

public class ModConfig {
	public static ModConfigFile getCommonConfig() {
		return MinecraftCommonAccess.get().getConfig();
	}

	public static ModConfigFile getWorldConfig() {
		return MinecraftServerAccess.get().getConfig();
	}
}
