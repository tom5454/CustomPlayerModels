package com.tom.cpm.mixinplugin;

import net.neoforged.fml.loading.LoadingModList;

public class MixinModLoaded {

	public static boolean isLoaded(String id) {
		return LoadingModList.get().getModFileById(id) != null;
	}

}
