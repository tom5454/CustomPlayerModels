package com.tom.cpm.mixinplugin;

public class RCDetector {

	public static boolean doApply() {
		return MixinModLoaded.isLoaded("realcamera");
	}
}
