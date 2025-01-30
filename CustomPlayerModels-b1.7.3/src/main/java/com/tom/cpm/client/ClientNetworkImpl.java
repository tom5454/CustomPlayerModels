package com.tom.cpm.client;

import net.minecraft.entity.Entity;
import net.modificationstation.stationapi.api.util.Identifier;

import com.tom.cpm.shared.network.NetH;

public interface ClientNetworkImpl extends NetH {
	void cpm$sendPacket(Identifier id, byte[] data);
	Entity cpm$getEntityByID(int id);
	String cpm$getConnectedServer();
}
