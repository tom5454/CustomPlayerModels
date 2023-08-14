package com.tom.cpm.shared;

import java.util.EnumSet;

import com.tom.cpl.block.BlockStateHandler;
import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.item.ItemStackHandler;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.api.CPMApiManager;
import com.tom.cpm.shared.util.IVersionCheck;
import com.tom.cpm.shared.util.VersionCheck;

public interface MinecraftCommonAccess {

	public static MinecraftCommonAccess get() {
		return MinecraftObjectHolder.commonObject;
	}

	ModConfigFile getConfig();
	public ILogger getLogger();
	public EnumSet<PlatformFeature> getSupportedFeatures();
	TextRemapper<?> getTextRemapper();
	CPMApiManager getApi();

	default String getPlatformVersionString() {
		return "Minecraft " + getMCVersion() + " " + getMCBrand() + " " + getModVersion();
	}

	String getMCVersion();
	String getMCBrand();
	String getModVersion();

	default IVersionCheck getVersionCheck() {
		return VersionCheck.get(getMCVersion(), getModVersion());
	}

	ItemStackHandler<?> getItemStackHandler();
	BlockStateHandler<?> getBlockStateHandler();
}
