package com.tom.cpm.client.optifine;

public class OptifineDetector {
	//Taken from Immersive Portals
	public static boolean detectOptiFine() {
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
