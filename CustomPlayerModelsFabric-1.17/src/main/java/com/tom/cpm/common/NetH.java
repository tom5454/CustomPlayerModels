package com.tom.cpm.common;

import net.minecraft.server.MinecraftServer;

import com.tom.cpm.shared.config.PlayerData;

public interface NetH {
	boolean cpm$hasMod();
	void cpm$setHasMod(boolean v);

	public static interface ServerNetH extends NetH {
		PlayerData cpm$getEncodedModelData();
		void cpm$setEncodedModelData(PlayerData data);

		MinecraftServer cpm$getServer();
	}
}
