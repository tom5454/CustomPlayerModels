package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.model.ModelBiped;

import com.tom.cpm.client.IBipedModel;

@Mixin(value = ModelBiped.class, remap = false)
public class BipedEntityModelMixin implements IBipedModel {
	private boolean cpm$noSetup;

	@Inject(at = @At("HEAD"), method = "setupAnimation(FFFFFF)V", cancellable = true)
	public void onSetAngles(float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale, CallbackInfo cbi) {
		if (cpm$noSetup)cbi.cancel();
	}

	@Override
	public void cpm$setNoSetup(boolean v) {
		cpm$noSetup = v;
	}

}
