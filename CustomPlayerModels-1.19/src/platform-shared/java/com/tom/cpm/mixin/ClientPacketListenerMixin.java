package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

import com.tom.cpl.util.NettyByteBufInputStream;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.network.NetH;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin implements NetH {
	private boolean cpm$hasMod;

	@Override
	public boolean cpm$hasMod() {
		return cpm$hasMod;
	}

	@Override
	public void cpm$setHasMod(boolean v) {
		this.cpm$hasMod = v;
	}

	@Inject(at = @At("HEAD"), method = "handleCustomPayload(Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;)V", cancellable = true)
	public void onHandleCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo cbi) {
		if(packet.getIdentifier().getNamespace().equals(MinecraftObjectHolder.NETWORK_ID)) {
			CustomPlayerModelsClient.INSTANCE.netHandler.receiveClient(packet.getIdentifier(), new NettyByteBufInputStream(packet.getData()), this);
			cbi.cancel();
		}
	}
}
