package com.tom.cpm.shared;

public interface MinecraftServerAccess {

	public static MinecraftServerAccess get() {
		return MinecraftObjectHolder.serverAccess;
	}
}
