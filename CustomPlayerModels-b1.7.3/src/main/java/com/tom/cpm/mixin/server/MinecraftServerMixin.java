package com.tom.cpm.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.class_182;
import net.minecraft.class_294;
import net.minecraft.server.MinecraftServer;

import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.server.ServerSidedHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.ModConfig;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

	@Inject(at = @At("HEAD"), method = "method_2170()V")
	private void onStopServer(CallbackInfo cbi) {
		ModConfig.getWorldConfig().save();
		MinecraftObjectHolder.setServerObject(null);
	}

	@Inject(at = @At(value = "NEW", target = "net/minecraft/class_73", shift = Shift.AFTER), method = "method_2159(Lnet/minecraft/class_182;Ljava/lang/String;J)V", locals = LocalCapture.CAPTURE_FAILHARD)
	public void onStartWorld(class_182 arg, String string, long l, CallbackInfo cbi, class_294 var5) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(var5));
	}

	static {
		CustomPlayerModels.proxy = new ServerSidedHandler();
	}
}
