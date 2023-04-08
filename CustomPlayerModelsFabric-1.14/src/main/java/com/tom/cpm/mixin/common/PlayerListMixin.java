package com.tom.cpm.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.dimension.DimensionType;

import com.tom.cpm.common.ServerHandler;

@Mixin(PlayerList.class)
public class PlayerListMixin {

	@Inject(at = @At("RETURN"), method = "placeNewPlayer(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/entity/player/ServerPlayerEntity;)V")
	public void onOnPlayerConnect(NetworkManager connection, ServerPlayerEntity player, CallbackInfo cbi) {
		ServerHandler.onPlayerJoin(player);
	}

	@Inject(method = "respawn(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/world/dimension/DimensionType;Z)Lnet/minecraft/entity/player/ServerPlayerEntity;", at = @At("TAIL"))
	private void afterRespawn(ServerPlayerEntity player, DimensionType dimension, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
		if(!alive)ServerHandler.netHandler.onRespawn(cir.getReturnValue());
	}
}
