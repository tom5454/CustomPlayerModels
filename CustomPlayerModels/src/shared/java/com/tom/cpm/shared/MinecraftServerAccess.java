package com.tom.cpm.shared;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpm.shared.network.NetHandler;

public interface MinecraftServerAccess {

	public static MinecraftServerAccess get() {
		return MinecraftObjectHolder.serverAccess;
	}

	ModConfigFile getConfig();
	NetHandler<?, ?, ?> getNetHandler();
}
