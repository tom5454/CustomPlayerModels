package com.tom.cpm;

import net.minecraft.core.entity.player.EntityPlayer;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.config.PlayerData;

public class PlayerDataExt extends PlayerData {
	private int skinLayer;

	public PlayerDataExt() {
	}

	public boolean hasModel() {
		return data == null;
	}

	public static int getSkinLayer(EntityPlayer player) {
		PlayerData pd = ServerHandler.netHandler.getSNetH(player).cpm$getEncodedModelData();
		return pd == null ? 0 : ((PlayerDataExt)pd).skinLayer;
	}

	public static void setSkinLayer(EntityPlayer player, int layer) {
		PlayerData pd = ServerHandler.netHandler.getSNetH(player).cpm$getEncodedModelData();
		((PlayerDataExt)pd).skinLayer = layer;
	}
}
