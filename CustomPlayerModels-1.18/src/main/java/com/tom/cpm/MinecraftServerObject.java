package com.tom.cpm;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpm.shared.MinecraftServerAccess;

public class MinecraftServerObject implements MinecraftServerAccess {
	public static final LevelResource CONFIG = new LevelResource("data/cpm.json");
	private MinecraftServer server;
	private ModConfigFile cfg;

	public MinecraftServerObject(MinecraftServer server) {
		this.server = server;
		cfg = new ModConfigFile(server.getWorldPath(CONFIG).toFile());
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}
}
