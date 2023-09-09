package com.tom.cpm;

import java.io.File;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;

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
		cfg = new ModConfigFile(new File(server.getLevel(DimensionType.OVERWORLD).getLevelStorage().getFolder(), "data/cpm.json"));
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

	@Override
	public BiomeHandler<?> getBiomeHandler() {
		return BiomeHandlerImpl.impl;
	}
}
