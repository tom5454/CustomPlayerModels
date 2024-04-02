package com.tom.cpm.common;

import net.minecraft.entity.player.PlayerEntity;
import net.modificationstation.stationapi.api.util.Identifier;

import com.tom.cpm.shared.network.NetH.ServerNetH;

public interface ServerNetworkImpl extends ServerNetH {
	void cpm$sendChat(String msg);
	PlayerEntity cpm$getPlayer();
	void cpm$kickPlayer(String msg);
	void cpm$sendPacket(Identifier id, byte[] data);
}