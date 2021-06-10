package com.tom.cpm.shared;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;

public interface MinecraftServerAccess {

	public static MinecraftServerAccess get() {
		return MinecraftObjectHolder.serverAccess;
	}

	ModConfigFile getConfig();
}
