package com.tom.cpm;

import net.minecraft.server.MinecraftServer;

import com.tom.cpl.block.BiomeHandler;
import com.tom.cpl.config.ModConfigFile;
import com.tom.cpm.common.BiomeHandlerImpl;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftServerAccess;
import com.tom.cpm.shared.network.NetHandler;

public class MinecraftServerObject implements MinecraftServerAccess {
	private MinecraftServer server;
	private ModConfigFile cfg;

	public MinecraftServerObject(MinecraftServer server) {
		this.server = server;
		cfg = ModConfigFile.createServer(server.getEntityWorld().getSaveHandler().getMapFileFromName("data/cpm.json"));
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}

	@Override
	public NetHandler<?, ?, ?> getNetHandler() {
		return ServerHandler.netHandler;
	}

	@Override
	public BiomeHandler<?> getBiomeHandler() {
		return BiomeHandlerImpl.impl;
	}
}
