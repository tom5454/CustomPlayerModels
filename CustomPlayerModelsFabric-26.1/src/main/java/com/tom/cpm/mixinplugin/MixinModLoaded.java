package com.tom.cpm.mixinplugin;

import net.fabricmc.loader.api.FabricLoader;

public class MixinModLoaded {

	public static boolean isLoaded(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}

}
