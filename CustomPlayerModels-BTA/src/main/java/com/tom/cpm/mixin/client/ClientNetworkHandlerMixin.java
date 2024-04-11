package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.net.handler.NetClientHandler;
import net.minecraft.client.world.WorldClient;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.net.packet.Packet;

import com.tom.cpm.client.ClientNetworkImpl;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.common.CustomPayload;
import com.tom.cpm.shared.io.FastByteArrayInputStream;

@Mixin(value = NetClientHandler.class, remap = false)
public abstract class ClientNetworkHandlerMixin implements ClientNetworkImpl {
	private @Shadow WorldClient worldClient;
	private boolean cpm$hasMod;

	public @Shadow abstract void addToSendQueue(final Packet packet);

	@Override
	public boolean cpm$hasMod() {
		return cpm$hasMod;
	}

	@Override
	public void cpm$setHasMod(boolean v) {
		cpm$hasMod = v;
	}

	@Override
	public void cpm$sendPacket(String id, byte[] data) {
		addToSendQueue(new CustomPayload(id, data));
	}

	@Override
	public Entity cpm$getEntityByID(int id) {
		return worldClient.getEntityById(id);
	}

	@Override
	public void cpm$processCustomPayload(CustomPayload p) {
		CustomPlayerModelsClient.netHandler.receiveClient(p.getId(), new FastByteArrayInputStream(p.getData()), this);
	}

}
