package com.tom.cpm.mixinplugin;

import net.neoforged.fml.loading.FMLLoader;

public class MixinModLoaded {

	public static boolean isLoaded(String id) {
		return FMLLoader.getCurrent().getLoadingModList().getModFileById(id) != null;
	}

}
