package com.tom.cpm;

import net.minecraft.entity.player.EntityPlayerMP;

import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpmcore.CPMASMClientHooks;

public class PlayerDataExt extends PlayerData {
	private int skinLayer;
	public PlayerDataExt(byte[] data, boolean forced, boolean save, int skinLayer) {
		super(data, forced, save);
		this.skinLayer = skinLayer;
	}

	public boolean hasModel() {
		return data == null;
	}

	public static int getSkinLayer(EntityPlayerMP player) {
		PlayerData pd = CPMASMClientHooks.getEncodedModelData(player.playerNetServerHandler);
		return pd == null ? 0 : ((PlayerDataExt)pd).skinLayer;
	}

	public static void setSkinLayer(EntityPlayerMP player, int layer) {
		PlayerData pd = CPMASMClientHooks.getEncodedModelData(player.playerNetServerHandler);
		if(pd == null) {
			pd = new PlayerDataExt(null, false, false, layer);
			CPMASMClientHooks.setEncodedModelData(player.playerNetServerHandler, pd);
		} else
			((PlayerDataExt)pd).skinLayer = layer;
	}
}
