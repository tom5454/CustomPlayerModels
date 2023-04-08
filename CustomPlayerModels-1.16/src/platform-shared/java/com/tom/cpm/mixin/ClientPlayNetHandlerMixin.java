package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;

import com.tom.cpl.util.NettyByteBufInputStream;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.Platform;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.network.NetH;

@Mixin(ClientPlayNetHandler.class)
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

	@Inject(at = @At("HEAD"), method = "handleCustomPayload(Lnet/minecraft/network/play/server/SCustomPayloadPlayPacket;)V", cancellable = true)
	public void onHandleCustomPayload(SCustomPayloadPlayPacket packet, CallbackInfo cbi) {
		if(Platform.getName(packet).getNamespace().equals(MinecraftObjectHolder.NETWORK_ID)) {
			CustomPlayerModelsClient.INSTANCE.netHandler.receiveClient(Platform.getName(packet), new NettyByteBufInputStream(Platform.getData(packet)), this);
			cbi.cancel();
		}
	}
}
