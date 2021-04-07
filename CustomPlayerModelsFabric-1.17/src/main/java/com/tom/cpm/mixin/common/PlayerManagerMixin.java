package com.tom.cpm.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import com.tom.cpm.common.ServerHandler;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

	@Inject(at = @At("RETURN"), method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V")
	public void onOnPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo cbi) {
		ServerHandler.onPlayerJoin(player);
	}
}
