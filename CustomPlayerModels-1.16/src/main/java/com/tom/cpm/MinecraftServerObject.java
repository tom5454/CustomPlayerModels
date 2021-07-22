package com.tom.cpm;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.FolderName;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpm.shared.MinecraftServerAccess;

public class MinecraftServerObject implements MinecraftServerAccess {
	public static final FolderName CONFIG = new FolderName("data/cpm.json");
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
