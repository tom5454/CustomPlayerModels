package com.tom.cpm.mixinplugin;

import org.quiltmc.loader.api.QuiltLoader;

public class MixinModLoaded {

	public static boolean isLoaded(String id) {
		return QuiltLoader.isModLoaded(id);
	}

}
