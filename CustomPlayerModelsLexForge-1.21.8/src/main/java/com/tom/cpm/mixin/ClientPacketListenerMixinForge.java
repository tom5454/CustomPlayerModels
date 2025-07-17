package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.common.ByteArrayPayload;
import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.network.NetH;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixinForge implements NetH {

	@Inject(at = @At("HEAD"), method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V", cancellable = true, require = 0)
	public void onHandleCustomPayload(CustomPacketPayload packet, CallbackInfo cbi) {
		if(packet instanceof ByteArrayPayload p) {
			CustomPlayerModelsClient.INSTANCE.netHandler.receiveClient((Type<ByteArrayPayload>) packet.type(), new FastByteArrayInputStream(p.data()), this);
			cbi.cancel();
		}
	}

}
