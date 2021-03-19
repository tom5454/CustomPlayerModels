package com.tom.cpm.shared.config;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpm.shared.MinecraftCommonAccess;

public class ModConfig {
	public static ModConfigFile getConfig() {
		return MinecraftCommonAccess.get().getConfig();
	}
}
