package com.tom.cpm.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;

import com.tom.cpm.common.ServerHandler;

@Mixin(ServerPlayer.class)
public class PlayerMixin {

	@Inject(at = @At("HEAD"), method = "jumpFromGround()V")
	public void onJump(CallbackInfo cbi) {
		ServerHandler.jump(this);
	}
}
