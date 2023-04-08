package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;

import com.tom.cpm.client.IBipedModel;

@Mixin(BipedModel.class)
public class BipedModelMixin implements IBipedModel {
	private boolean cpm$noSetup;

	@Inject(at = @At("HEAD"), method = "setupAnim(Lnet/minecraft/entity/LivingEntity;FFFFFF)V", cancellable = true)
	public void setupAnim(LivingEntity p_212844_1_, float p_212844_2_, float p_212844_3_, float p_212844_4_, float p_212844_5_, float p_212844_6_, float p_212844_7_, CallbackInfo cbi) {
		if(cpm$noSetup)cbi.cancel();
	}

	@Override
	public void cpm$noSetup() {
		this.cpm$noSetup = true;
	}
}
