package com.tom.cpm.common;

import net.minecraft.core.entity.player.EntityPlayer;

import com.tom.cpm.shared.network.NetH.ServerNetH;

public interface ServerNetworkImpl extends ServerNetH, CPMPayloadHandler {
	void cpm$sendChat(String msg);
	EntityPlayer cpm$getPlayer();
	void cpm$kickPlayer(String msg);
	void cpm$sendPacket(String id, byte[] data);
}