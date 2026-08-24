package com.tom.cpmflashback.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.moulberry.flashback.Flashback;

import com.tom.cpm.shared.animation.AnimationEngine;

@Mixin(AnimationEngine.class)
public class AnimationEngineMixin {
	private @Shadow(remap = false) long tickCounter;
	private @Shadow(remap = false) float partial;

	@Inject(at = @At("HEAD"), method = "tick()V", remap = false)
	private void onTick(CallbackInfo cbi) {
		var r = Flashback.getReplayServer();
		if (r != null) {
			tickCounter = r.getReplayTick() - 1;//CPM has a tickCounter++ after this
		}
	}

	@Inject(at = @At("HEAD"), method = "update(F)V", remap = false, cancellable = true)
	private void onUpdate(float partial, CallbackInfo cbi) {
		var r = Flashback.getReplayServer();
		if (r != null && r.replayPaused) {
			partial = 0f;
			cbi.cancel();
		}
	}
}
