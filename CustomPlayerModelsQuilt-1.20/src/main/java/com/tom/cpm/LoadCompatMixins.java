package com.tom.cpm;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.spongepowered.asm.mixin.Mixins;

public class LoadCompatMixins implements PreLaunchEntrypoint {

	@Override
	public void onPreLaunch(ModContainer mod) {
		Mixins.addConfiguration("cpm.mixins.compat.json");
	}
}