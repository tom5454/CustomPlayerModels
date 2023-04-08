package com.tom.cpm.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerList;

import com.tom.cpm.common.ServerHandler;

@Mixin(PlayerList.class)
public class PlayerListMixin {

	@Inject(at = @At("RETURN"), method = "placeNewPlayer(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/entity/player/ServerPlayerEntity;)V")
	public void onOnPlayerConnect(NetworkManager connection, ServerPlayerEntity player, CallbackInfo cbi) {
		ServerHandler.onPlayerJoin(player);
	}
}
