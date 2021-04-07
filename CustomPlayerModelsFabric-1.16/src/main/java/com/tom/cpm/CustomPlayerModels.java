package com.tom.cpm;

import java.io.File;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpm.common.CommandCPM;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;

public class CustomPlayerModels implements MinecraftCommonAccess, ModInitializer {
	private ModConfigFile config;

	@Override
	public void onInitialize() {
		config = new ModConfigFile(new File(FabricLoader.getInstance().getConfigDir().toFile(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);

		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			MinecraftObjectHolder.setServerObject(new MinecraftServerObject(s));
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
			MinecraftObjectHolder.setServerObject(null);
		});
		CommandRegistrationCallback.EVENT.register((d, isD) -> {
			CommandCPM.register(d);
		});
	}

	@Override
	public ModConfigFile getConfig() {
		return config;
	}
}
