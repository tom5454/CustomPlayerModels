package com.tom.cpm;

import java.io.File;

import net.minecraft.server.MinecraftServer;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpm.shared.MinecraftServerAccess;

public class MinecraftServerObject implements MinecraftServerAccess {
	private MinecraftServer server;
	private ModConfigFile cfg;

	public MinecraftServerObject(MinecraftServer server) {
		this.server = server;
		cfg = new ModConfigFile(new File(server.getEntityWorld().getSaveHandler().getWorldDirectory(), "data/cpm.json"));
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}
}
