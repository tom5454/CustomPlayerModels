package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;

import com.tom.cpm.client.CustomPlayerModelsClient;
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

	@Inject(at = @At("RETURN"), method = "handleConfigurationStart(Lnet/minecraft/network/protocol/game/ClientboundStartConfigurationPacket;)V", cancellable = true)
	public void onReconfigure(ClientboundStartConfigurationPacket packet, CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.onLogOut();
	}
}
