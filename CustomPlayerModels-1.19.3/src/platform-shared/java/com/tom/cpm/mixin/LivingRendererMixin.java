package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

import com.tom.cpm.client.LivingRendererAccess;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements LivingRendererAccess {

	protected LivingRendererMixin(Context p_174008_) {
		super(p_174008_);
	}

	@Inject(at = @At("HEAD"), method = "getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;", cancellable = true)
	public void onGetRenderType(LivingEntity player, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing, CallbackInfoReturnable<RenderType> cbi) {
		if(!pBodyVisible) {
			cpm$onGetRenderType(player, pTranslucent, pGlowing, cbi);
		}
	}

	@Override
	public void cpm$onGetRenderType(LivingEntity player, boolean pTranslucent, boolean pGlowing, CallbackInfoReturnable<RenderType> cbi) {}
}
