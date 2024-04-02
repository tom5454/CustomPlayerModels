package com.tom.cpm.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.class_174;
import net.minecraft.class_69;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.network.NetH.ServerNetH;

@Mixin(class_174.class)
public class EntityTrackerEntryMixin {
	public @Shadow Entity field_597;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;method_943()Z"), method = "method_601(Lnet/minecraft/class_69;)V")
	public void onStartTracking(class_69 trackingPlayer, CallbackInfo cbi) {
		ServerPlayNetworkHandler handler = trackingPlayer.field_255;
		if (((ServerNetH) handler).cpm$hasMod()) {
			ServerHandler.netHandler.sendPlayerData((PlayerEntity) field_597, trackingPlayer);
		}
	}
}
