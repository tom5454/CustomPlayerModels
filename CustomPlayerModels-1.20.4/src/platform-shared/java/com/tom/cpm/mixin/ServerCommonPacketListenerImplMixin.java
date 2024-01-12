package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

import com.tom.cpm.common.ByteArrayPayload;
import com.tom.cpm.common.ServerNetHandler;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin implements ServerNetHandler {

	@Inject(at = @At("HEAD"), method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ServerboundCustomPayloadPacket;)V", cancellable = true)
	public void onProcessCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo cbi) {
		if(packet.payload() instanceof ByteArrayPayload p) {
			cpm$handleCustomPayload(p);
			cbi.cancel();
		}
	}
}
