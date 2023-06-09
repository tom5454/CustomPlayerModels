package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;

import com.tom.cpm.client.LivingRendererAccess;

@Mixin(LivingRenderer.class)
public abstract class LivingRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements IEntityRenderer<T, M>, LivingRendererAccess {

	protected LivingRendererMixin(EntityRendererManager p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Inject(at = @At("HEAD"), method = "getRenderType(Lnet/minecraft/entity/LivingEntity;ZZ)Lnet/minecraft/client/renderer/RenderType;", cancellable = true)
	public void onGetRenderType(LivingEntity player, boolean pBodyVisible, boolean pTranslucent, CallbackInfoReturnable<RenderType> cbi) {
		if(!pBodyVisible) {
			cpm$onGetRenderType(player, pTranslucent, cbi);
		}
	}

	@Override
	public void cpm$onGetRenderType(LivingEntity player, boolean pTranslucent, CallbackInfoReturnable<RenderType> cbi) {}
}
