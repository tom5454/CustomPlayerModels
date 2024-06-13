package com.tom.cpm.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.server.entity.EntityTrackerEntry;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.server.net.handler.NetServerHandler;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.network.NetH.ServerNetH;

@Mixin(value = EntityTrackerEntry.class, remap = false)
public class EntityTrackerEntryMixin {
	public @Shadow Entity trackedEntity;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/entity/player/EntityPlayer;isPlayerSleeping()Z"), method = "updatePlayerEntity(Lnet/minecraft/server/entity/player/EntityPlayerMP;)V")
	public void onStartTracking(EntityPlayerMP trackingPlayer, CallbackInfo cbi) {
		NetServerHandler handler = trackingPlayer.playerNetServerHandler;
		if (((ServerNetH) handler).cpm$hasMod()) {
			ServerHandler.netHandler.sendPlayerData((EntityPlayer) trackedEntity, trackingPlayer);
		}
	}
}
