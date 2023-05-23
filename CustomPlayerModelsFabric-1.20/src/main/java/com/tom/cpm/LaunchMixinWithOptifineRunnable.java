package com.tom.cpm;

import org.spongepowered.asm.mixin.Mixins;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class LaunchMixinWithOptifineRunnable implements PreLaunchEntrypoint {

	@Override
	public void onPreLaunch() {
		Mixins.addConfiguration("cpm.mixins.of.json");
	}
}