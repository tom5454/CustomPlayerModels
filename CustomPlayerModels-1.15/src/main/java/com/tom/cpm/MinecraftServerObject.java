package com.tom.cpm;

import net.minecraft.server.MinecraftServer;

import com.tom.cpm.shared.MinecraftServerAccess;

public class MinecraftServerObject implements MinecraftServerAccess {
	private MinecraftServer server;

	public MinecraftServerObject(MinecraftServer server) {
		this.server = server;
	}

}
