package com.tom.cpm.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.server.entity.EntityTrackerEntryImpl;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.network.NetH.ServerNetH;

@Mixin(value = EntityTrackerEntryImpl.class, remap = false)
public class EntityTrackerEntryMixin {
	public @Shadow Entity trackedEntity;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/entity/player/PlayerServer;isPlayerSleeping()Z"), method = "updatePlayerEntity(Lnet/minecraft/core/entity/player/Player;)V")
	public void onStartTracking(Player trackingPlayer, CallbackInfo cbi) {
		PacketHandlerServer handler = ((PlayerServer) trackingPlayer).playerNetServerHandler;
		if (((ServerNetH) handler).cpm$hasMod()) {
			ServerHandler.netHandler.sendPlayerData((Player) trackedEntity, trackingPlayer);
		}
	}
}
