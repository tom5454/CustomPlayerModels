package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.GameRenderer;

import com.tom.cpm.client.PlayerProfile;

@Mixin(GameRenderer.class)
public class GameRendererMixinFabric {

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V"))
	private void onBeforeRenderScreen(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		PlayerProfile.inGui = true;
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V", shift = At.Shift.AFTER))
	private void onAfterRenderScreen(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		PlayerProfile.inGui = false;
	}
}
