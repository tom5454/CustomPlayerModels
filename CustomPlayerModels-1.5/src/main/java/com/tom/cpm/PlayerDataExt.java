package com.tom.cpm;

import net.minecraft.entity.player.EntityPlayerMP;

import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.network.NetH.ServerNetH;

public class PlayerDataExt extends PlayerData {
	private int skinLayer;

	public PlayerDataExt() {
	}

	public boolean hasModel() {
		return data == null;
	}

	public static int getSkinLayer(EntityPlayerMP player) {
		PlayerData pd = ((ServerNetH)player.playerNetServerHandler).cpm$getEncodedModelData();
		return pd == null ? 0 : ((PlayerDataExt)pd).skinLayer;
	}

	public static void setSkinLayer(EntityPlayerMP player, int layer) {
		PlayerData pd = ((ServerNetH)player.playerNetServerHandler).cpm$getEncodedModelData();
		((PlayerDataExt)pd).skinLayer = layer;
	}
}
