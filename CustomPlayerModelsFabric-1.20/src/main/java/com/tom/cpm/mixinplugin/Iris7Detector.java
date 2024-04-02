package com.tom.cpm.mixinplugin;

public class Iris7Detector {

	public static boolean doApply() {
		if (!MixinModLoaded.isLoaded("iris"))return false;

		try {
			Class.forName("net.irisshaders.batchedentityrendering.impl.BlendingStateHolder");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
