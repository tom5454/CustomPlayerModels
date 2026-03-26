package com.tom.cpm.mixinplugin;

public class OFDetector {

	public static boolean doApply() {
		return MixinModLoaded.isLoaded("optifabric");
	}
}
