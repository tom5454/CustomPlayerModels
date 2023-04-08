package com.tom.cpm;

import org.spongepowered.asm.mixin.Mixins;

import net.fabricmc.loader.api.FabricLoader;

public class LaunchMixinWithOptifineRunnable implements Runnable {

	//Taken from Immersive Portals
	@Override
	public void run() {
		if (FabricLoader.getInstance().isModLoaded("optifabric")) {
			System.out.println("Registering Mixin for OptiFine");
			Mixins.addConfiguration("cpm.mixins.of.json");
		}
	}
}