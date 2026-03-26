package com.tom.cpm.mixinplugin;

public class OFDetector {

	//Taken from Immersive Portals
	public static boolean doApply() {
		try {
			//do not load other optifine classes that loads vanilla classes
			//that would load the class before mixin
			Class.forName("optifine.ZipResourceProvider");
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}
}
