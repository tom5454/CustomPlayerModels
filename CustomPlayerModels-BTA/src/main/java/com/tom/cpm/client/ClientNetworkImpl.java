package com.tom.cpm.client;

import net.minecraft.core.entity.Entity;

import com.tom.cpm.common.CPMPayloadHandler;
import com.tom.cpm.shared.network.NetH;

public interface ClientNetworkImpl extends NetH, CPMPayloadHandler {
	void cpm$sendPacket(String id, byte[] data);
	Entity cpm$getEntityByID(int id);
}
