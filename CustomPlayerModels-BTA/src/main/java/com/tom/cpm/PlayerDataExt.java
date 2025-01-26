package com.tom.cpm;

import net.minecraft.core.entity.player.Player;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.config.PlayerData;

public class PlayerDataExt extends PlayerData {
	private int skinLayer;

	public PlayerDataExt() {
	}

	public boolean hasModel() {
		return data == null;
	}

	public static int getSkinLayer(Player player) {
		PlayerData pd = ServerHandler.netHandler.getSNetH(player).cpm$getEncodedModelData();
		return pd == null ? 0 : ((PlayerDataExt)pd).skinLayer;
	}

	public static void setSkinLayer(Player player, int layer) {
		PlayerData pd = ServerHandler.netHandler.getSNetH(player).cpm$getEncodedModelData();
		((PlayerDataExt)pd).skinLayer = layer;
	}
}
