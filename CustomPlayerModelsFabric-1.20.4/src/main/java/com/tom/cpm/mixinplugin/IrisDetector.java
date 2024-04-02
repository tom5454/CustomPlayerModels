package com.tom.cpm.mixinplugin;

public class IrisDetector {

	public static boolean doApply() {
		return MixinModLoaded.isLoaded("iris") && !Iris7Detector.doApply();
	}
}
