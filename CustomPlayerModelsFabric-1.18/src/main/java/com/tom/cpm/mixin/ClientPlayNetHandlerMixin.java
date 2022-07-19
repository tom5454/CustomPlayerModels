package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;

import com.tom.cpl.util.NettyByteBufInputStream;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.network.NetH;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetHandlerMixin implements NetH {
	private boolean cpm$hasMod;

	@Override
	public boolean cpm$hasMod() {
		return cpm$hasMod;
	}

	@Override
	public void cpm$setHasMod(boolean v) {
		this.cpm$hasMod = v;
	}

	@Inject(at = @At("HEAD"), method = "onCustomPayload(Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;)V", cancellable = true)
	public void onHandleCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo cbi) {
		if(packet.getChannel().getNamespace().equals(MinecraftObjectHolder.NETWORK_ID)) {
			CustomPlayerModelsClient.INSTANCE.netHandler.receiveClient(packet.getChannel(), new NettyByteBufInputStream(packet.getData()), this);
			cbi.cancel();
		}
	}
}
