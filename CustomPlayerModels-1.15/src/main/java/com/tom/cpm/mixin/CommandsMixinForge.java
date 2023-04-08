package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import com.mojang.brigadier.CommandDispatcher;

import com.tom.cpm.common.ServerHandler;

@Mixin(Commands.class)
public class CommandsMixinForge {
	private @Shadow @Final CommandDispatcher<CommandSource> dispatcher;

	@Inject(at = @At("RETURN"), method = "<init>(Z)V")
	public void onInit(boolean dedicated, CallbackInfo cbi) {
		ServerHandler.registerCommands(dispatcher);
	}
}
