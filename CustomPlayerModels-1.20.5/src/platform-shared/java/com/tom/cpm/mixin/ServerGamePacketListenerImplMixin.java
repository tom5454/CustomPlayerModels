package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import com.tom.cpm.common.ByteArrayPayload;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.network.NetH.ServerNetH;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements ServerNetH {
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

	@Inject(at = @At("HEAD"), method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ServerboundCustomPayloadPacket;)V", cancellable = true)
	public void onProcessCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo cbi) {
		if(packet.payload() instanceof ByteArrayPayload p) {
			ServerHandler.netHandler.receiveServer(p.id(), new FastByteArrayInputStream(p.data()), this);
			cbi.cancel();
		}
	}
}
