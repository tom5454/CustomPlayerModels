package com.tom.cpm;

import net.minecraft.class_52;

import com.tom.cpl.block.BiomeHandler;
import com.tom.cpl.config.ModConfigFile;
import com.tom.cpm.common.BiomeHandlerImpl;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftServerAccess;
import com.tom.cpm.shared.network.NetHandler;

public class MinecraftServerObject implements MinecraftServerAccess {
	private ModConfigFile cfg;

	public MinecraftServerObject(class_52 handler) {
		cfg = ModConfigFile.createServer(handler.method_1736("data/cpm.json"));
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
