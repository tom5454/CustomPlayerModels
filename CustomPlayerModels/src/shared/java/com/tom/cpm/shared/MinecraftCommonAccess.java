package com.tom.cpm.shared;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpl.util.ILogger;

public interface MinecraftCommonAccess {

	public static MinecraftCommonAccess get() {
		return MinecraftObjectHolder.commonObject;
	}

	ModConfigFile getConfig();
	public ILogger getLogger();
}
