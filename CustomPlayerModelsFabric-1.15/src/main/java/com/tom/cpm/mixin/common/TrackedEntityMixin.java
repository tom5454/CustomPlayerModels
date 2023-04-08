package com.tom.cpm.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.TrackedEntity;

import com.tom.cpm.common.ServerHandler;

@Mixin(TrackedEntity.class)
public class TrackedEntityMixin {

	private @Shadow @Final Entity entity;

	@Inject(method = "addPairing(Lnet/minecraft/entity/player/ServerPlayerEntity;)V", at = @At("RETURN"))
	private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
		ServerHandler.onTrackingStart(entity, player);
	}
}
