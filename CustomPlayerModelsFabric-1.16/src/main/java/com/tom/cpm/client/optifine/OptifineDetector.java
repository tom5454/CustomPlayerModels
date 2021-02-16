package com.tom.cpm.client.optifine;

import net.fabricmc.loader.api.FabricLoader;

public class OptifineDetector {
	//Taken from Immersive Portals
	public static boolean detectOptiFine() {
		boolean isOptiFabricPresent = FabricLoader.getInstance().isModLoaded("optifabric");

		if (!isOptiFabricPresent) {
			return false;
		}

		try {
			//do not load other optifine classes that loads vanilla classes
			//that would load the class before mixin
			Class.forName("optifine.ZipResourceProvider");
			return true;
		}
		catch (ClassNotFoundException e) {
			System.err.println("OptiFabric is present but OptiFine is not present!!!");
			return false;
		}
	}
}
