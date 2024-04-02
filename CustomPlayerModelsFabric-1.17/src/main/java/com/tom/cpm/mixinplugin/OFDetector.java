package com.tom.cpm.mixinplugin;

import net.fabricmc.loader.api.FabricLoader;

public class OFDetector {

	public static boolean doApply() {
		return FabricLoader.getInstance().isModLoaded("optifabric");
	}
}
