package com.tom.cpm.common;

import net.minecraft.core.entity.player.Player;

import com.tom.cpm.shared.network.NetH.ServerNetH;

public interface ServerNetworkImpl extends ServerNetH {
	void cpm$sendChat(String msg);
	Player cpm$getPlayer();
	void cpm$kickPlayer(String msg);
	void cpm$sendPacket(String id, byte[] data);
}