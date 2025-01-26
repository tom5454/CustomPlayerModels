package com.tom.cpm.mixin.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.client.world.WorldClientMP;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.net.NetworkManager;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.PacketCustomPayload;

import com.tom.cpm.client.ClientNetworkImpl;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.io.FastByteArrayInputStream;

@Mixin(value = PacketHandlerClient.class, remap = false)
public abstract class ClientNetworkHandlerMixin implements ClientNetworkImpl {
	private @Shadow @Final Minecraft mc;
	private @Shadow WorldClientMP worldClientMP;
	private @Shadow @Final NetworkManager netManager;
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
		addToSendQueue(new PacketCustomPayload(id, data));
	}

	@Override
	public Entity cpm$getEntityByID(int id) {
		if (mc.thePlayer.id == id)return mc.thePlayer;
		return worldClientMP.getEntityFromId(id);
	}

	@Override
	public String cpm$getConnectedServer() {
		SocketAddress sa = netManager.getRemoteAddress();
		if (sa instanceof InetSocketAddress)
			return ((InetSocketAddress) sa).getHostString();
		return null;
	}

	@Inject(at = @At("HEAD"), method = "handleCustomPayload", cancellable = true)
	public void onCustomPayload(PacketCustomPayload customPayloadPacket, CallbackInfo cbi) {
		if (customPayloadPacket.channel.startsWith(MinecraftObjectHolder.NETWORK_ID + ":")) {
			CustomPlayerModelsClient.netHandler.receiveClient(customPayloadPacket.channel, new FastByteArrayInputStream(customPayloadPacket.data), this);
			cbi.cancel();
		}
	}
}
