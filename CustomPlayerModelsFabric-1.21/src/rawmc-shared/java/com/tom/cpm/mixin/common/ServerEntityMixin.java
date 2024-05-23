package com.tom.cpm.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.tom.cpm.common.ServerHandler;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {

	private @Shadow @Final Entity entity;

	@Inject(method = "addPairing(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("RETURN"))
	private void onStartTracking(ServerPlayer player, CallbackInfo ci) {
		ServerHandler.onTrackingStart(entity, player);
	}
}
