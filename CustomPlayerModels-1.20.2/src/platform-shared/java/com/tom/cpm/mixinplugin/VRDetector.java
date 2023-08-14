package com.tom.cpm.mixinplugin;

public class VRDetector {

	public static boolean doApply() {
		try {
			//do not load other vivecraft classes that loads vanilla classes
			//that would load the class before mixin
			Class.forName("org.vivecraft.common.utils.lwjgl.Matrix");
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}
}
