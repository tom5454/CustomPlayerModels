package com.tom.cpm;

import java.io.File;

import net.minecraft.server.MinecraftServer;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftServerAccess;
import com.tom.cpm.shared.network.NetHandler;

public class MinecraftServerObject implements MinecraftServerAccess {
	private MinecraftServer server;
	private ModConfigFile cfg;

	public MinecraftServerObject(MinecraftServer server) {
		this.server = server;
		cfg = ModConfigFile.createServer(new File(server.getEntityWorld().getSaveHandler().getWorldDirectory(), "data/cpm.json"));
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}

	@Override
	public NetHandler<?, ?, ?> getNetHandler() {
		return ServerHandler.netHandler;
	}
}
