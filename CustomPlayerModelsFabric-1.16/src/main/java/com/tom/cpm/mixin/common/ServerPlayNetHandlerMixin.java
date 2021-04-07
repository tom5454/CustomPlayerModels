package com.tom.cpm.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.impl.networking.CustomPayloadC2SPacketAccessor;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import com.tom.cpm.common.NetH;
import com.tom.cpm.common.NetH.ServerNetH;
import com.tom.cpm.common.NetworkHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.PlayerData;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetHandlerMixin implements NetH, ServerNetH {

	@Shadow @Final MinecraftServer server;

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

	@Inject(at = @At("HEAD"), method = "onCustomPayload(Lnet/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;)V", cancellable = true)
	public void onProcessCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo cbi) {
		if(((CustomPayloadC2SPacketAccessor) packet).getChannel().getNamespace().equals(MinecraftObjectHolder.NETWORK_ID)) {
			NetworkHandler.handlePacket(packet, this, false);
			cbi.cancel();
		}
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
	public MinecraftServer cpm$getServer() {
		return server;
	}
}
