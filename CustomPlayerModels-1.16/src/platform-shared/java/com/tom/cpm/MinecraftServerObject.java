package com.tom.cpm;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.FolderName;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftServerAccess;
import com.tom.cpm.shared.network.NetHandler;

public class MinecraftServerObject implements MinecraftServerAccess {
	public static final FolderName CONFIG = new FolderName("data/cpm.json");
	private MinecraftServer server;
	private ModConfigFile cfg;

	public MinecraftServerObject(MinecraftServer server) {
		this.server = server;
		cfg = ModConfigFile.createServer(server.getWorldPath(CONFIG).toFile());
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}

	@Override
	public NetHandler<?, ?, ?> getNetHandler() {
		return ServerHandler.netHandler;
	}

	public MinecraftServer getServer() {
		return server;
	}
}
