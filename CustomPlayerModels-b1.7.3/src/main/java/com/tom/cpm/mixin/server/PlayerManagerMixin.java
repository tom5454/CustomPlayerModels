package com.tom.cpm.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.class_166;
import net.minecraft.class_69;

import com.tom.cpm.common.ServerHandler;

@Mixin(class_166.class)
public class PlayerManagerMixin {

	@Inject(at = @At("RETURN"), method = "method_569(Lnet/minecraft/class_69;)V")
	public void onLogin(class_69 arg, CallbackInfo cbi) {
		ServerHandler.netHandler.onJoin(arg);
	}
}
