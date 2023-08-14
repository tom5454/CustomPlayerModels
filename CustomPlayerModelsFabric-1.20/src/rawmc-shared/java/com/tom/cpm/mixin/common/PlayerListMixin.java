package com.tom.cpm.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import com.tom.cpm.common.ServerHandler;

@Mixin(PlayerList.class)
public class PlayerListMixin {

	@Inject(at = @At("RETURN"), method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V")
	public void onOnPlayerConnect(Connection connection, ServerPlayer player, CallbackInfo cbi) {
		ServerHandler.onPlayerJoin(player);
	}
}
