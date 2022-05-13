package com.tom.cpm;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftServerAccess;
import com.tom.cpm.shared.network.NetHandler;

public class MinecraftServerObject implements MinecraftServerAccess {
	public static final WorldSavePath CONFIG = new WorldSavePath("data/cpm.json");
	private MinecraftServer server;
	private ModConfigFile cfg;

	public MinecraftServerObject(MinecraftServer server) {
		this.server = server;
		cfg = ModConfigFile.createServer(server.getSavePath(CONFIG).toFile());
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}

	@Override
	public NetHandler<?, ?, ?> getNetHandler() {
		return ServerHandler.netHandler;
	}

	public static MinecraftServer getServer() {
		return ((MinecraftServerObject)MinecraftServerAccess.get()).server;
	}
}
