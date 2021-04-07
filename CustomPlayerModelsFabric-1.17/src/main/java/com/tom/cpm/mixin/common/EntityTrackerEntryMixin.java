package com.tom.cpm.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import com.tom.cpm.common.ServerHandler;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {

	@Shadow @Final Entity entity;

	@Inject(at = @At("RETURN"), method = "startTracking(Lnet/minecraft/server/network/ServerPlayerEntity;)V")
	public void startTracking(ServerPlayerEntity player, CallbackInfo cbi) {
		ServerHandler.onTrackingStart(player, entity);
	}
}
