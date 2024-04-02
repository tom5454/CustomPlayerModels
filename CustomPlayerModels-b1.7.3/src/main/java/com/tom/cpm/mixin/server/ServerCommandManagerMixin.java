package com.tom.cpm.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.class_38;
import net.minecraft.class_426;

import com.tom.cpm.server.ServerSidedHandler;

@Mixin(class_426.class)
public class ServerCommandManagerMixin {

	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;info(Ljava/lang/String;)V", ordinal = 3), method = "method_1411(Lnet/minecraft/class_38;)V", cancellable = true)
	public void onRunCommand(class_38 arg, CallbackInfo cbi) {
		if (ServerSidedHandler.cpm.onCommand(arg.field_160, arg.field_159)) {
			cbi.cancel();
		}
	}
}
