package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CCustomPayloadPacket;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.network.NetH.ServerNetH;

@Mixin(ServerPlayNetHandler.class)
public class ServerPlayNetHandlerMixin implements ServerNetH {
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

	@Inject(at = @At("HEAD"), method = "processCustomPayload(Lnet/minecraft/network/play/client/CCustomPayloadPacket;)V", cancellable = true)
	public void onProcessCustomPayload(CCustomPayloadPacket packet, CallbackInfo cbi) {
		if(packet.getName().getNamespace().equals(MinecraftObjectHolder.NETWORK_ID)) {
			ServerHandler.netHandler.receiveServer(packet.getName(), packet.getInternalData(), this);
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
}
