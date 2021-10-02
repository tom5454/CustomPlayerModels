package com.tom.cpm.shared;

import java.util.EnumSet;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpl.util.ILogger;

public interface MinecraftCommonAccess {

	public static MinecraftCommonAccess get() {
		return MinecraftObjectHolder.commonObject;
	}

	ModConfigFile getConfig();
	public ILogger getLogger();
	public EnumSet<PlatformFeature> getSupportedFeatures();
	String getPlatformVersionString();
}
