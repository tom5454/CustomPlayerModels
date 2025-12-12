package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import com.tom.cpm.common.ByteArrayPayload;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.network.NetH.ServerNetH;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixinForge implements ServerNetH {

	@Inject(at = @At("HEAD"), method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ServerboundCustomPayloadPacket;)V", cancellable = true)
	public void onProcessCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo cbi) {
		if(packet.payload() instanceof ByteArrayPayload p) {
			ServerHandler.netHandler.receiveServer(p.id(), new FastByteArrayInputStream(p.data()), this);
			cbi.cancel();
		}
	}

}
