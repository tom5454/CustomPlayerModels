package com.tom.cpm.mixinplugin;

public class FPMDetector {

	public static boolean doApply() {
		return MixinModLoaded.isLoaded("firstperson");
	}
}
