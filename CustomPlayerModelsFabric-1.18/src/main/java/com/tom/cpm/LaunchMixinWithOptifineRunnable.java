package com.tom.cpm;

import org.spongepowered.asm.mixin.Mixins;

public class LaunchMixinWithOptifineRunnable implements Runnable {

	//Taken from Immersive Portals
	@Override
	public void run() {
		Mixins.addConfiguration("cpm.mixins.of.json");
	}
}