package com.tom.cpm.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.world.save.ISaveFormat;
import net.minecraft.core.world.save.SaveHandlerServer;
import net.minecraft.server.MinecraftServer;

import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.ModConfig;

@Mixin(value = MinecraftServer.class, remap = false)
public class MinecraftServerMixin {

	@Inject(at = @At("HEAD"), method = "stopServer()V")
	private void onStopServer(CallbackInfo cbi) {
		ModConfig.getWorldConfig().save();
		MinecraftObjectHolder.setServerObject(null);
	}

	@Inject(at = @At(value = "NEW", target = "net/minecraft/server/world/WorldServer", shift = Shift.AFTER), method = "initWorld(Lnet/minecraft/core/world/save/ISaveFormat;Ljava/lang/String;J)V", locals = LocalCapture.CAPTURE_FAILHARD)
	public void onStartWorld(ISaveFormat arg, String string, long l, CallbackInfo cbi, SaveHandlerServer var5) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(var5));
	}

}
