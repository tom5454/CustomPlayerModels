package com.tom.cpm;

import java.io.File;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.ConfigEntry.ModConfig;

public class CustomPlayerModels implements MinecraftCommonAccess, ModInitializer {
	private ModConfig config;

	@Override
	public void onInitialize() {
		config = new ModConfig(new File(FabricLoader.getInstance().getConfigDir().toFile(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);

		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			MinecraftObjectHolder.setServerObject(new MinecraftServerObject(s));
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
			MinecraftObjectHolder.setServerObject(null);
		});
	}

	@Override
	public ModConfig getConfig() {
		return config;
	}
}
