package com.tom.cpm.shared;

import java.util.EnumSet;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.api.CPMApiManager;

public interface MinecraftCommonAccess {

	public static MinecraftCommonAccess get() {
		return MinecraftObjectHolder.commonObject;
	}

	ModConfigFile getConfig();
	public ILogger getLogger();
	public EnumSet<PlatformFeature> getSupportedFeatures();
	String getPlatformVersionString();
	TextRemapper<?> getTextRemapper();
	CPMApiManager getApi();
}
