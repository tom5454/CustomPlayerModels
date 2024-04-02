package com.tom.cpm.client;

import net.minecraft.src.Entity;

import com.tom.cpm.shared.network.NetH;

public interface ClientNetworkImpl extends NetH {
	void cpm$sendPacket(String id, byte[] data);
	Entity cpm$getEntityByID(int id);
}
