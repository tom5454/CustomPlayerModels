package com.tom.cpm.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.PacketChat;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.common.ServerNetworkImpl;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.io.FastByteArrayInputStream;

@Mixin(value = PacketHandlerServer.class, remap = false)
public abstract class ServerNetworkHandlerMixin implements ServerNetworkImpl {

	private @Shadow PlayerServer playerEntity;
	public @Shadow abstract void sendPacket(Packet arg);
	public @Shadow abstract void kickPlayer(String string);

	private boolean cpm$hasMod;
	private PlayerData cpm$data;

	@Override
	public boolean cpm$hasMod() {
		return cpm$hasMod;
	}

	@Override
	public void cpm$setHasMod(boolean v) {
		this.cpm$hasMod = v;
	}

	@Override
	public PlayerData cpm$getEncodedModelData() {
		return cpm$data;
	}

	@Override
	public void cpm$setEncodedModelData(PlayerData data) {
		cpm$data = data;
	}

	@Override
	public void cpm$sendChat(String msg) {
		sendPacket(new PacketChat(msg));
	}

	@Override
	public Player cpm$getPlayer() {
		return playerEntity;
	}

	@Override
	public void cpm$kickPlayer(String msg) {
		kickPlayer(msg);
	}

	@Override
	public void cpm$sendPacket(String id, byte[] data) {
		sendPacket(new PacketCustomPayload(id, data));
	}

	@Inject(at = @At("HEAD"), method = "handleCustomPayload", cancellable = true)
	public void onCustomPayload(PacketCustomPayload customPayloadPacket, CallbackInfo cbi) {
		if (customPayloadPacket.channel.startsWith(MinecraftObjectHolder.NETWORK_ID + ":")) {
			ServerHandler.netHandler.receiveServer(customPayloadPacket.channel, new FastByteArrayInputStream(customPayloadPacket.data), this);
			cbi.cancel();
		}
	}
}
