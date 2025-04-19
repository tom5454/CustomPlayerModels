package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.LivingRendererAccessFabric;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingRendererMixinFabric<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> implements LivingRendererAccessFabric {

	protected LivingRendererMixinFabric(Context p_174008_) {
		super(p_174008_);
	}

	@Inject(at = @At("HEAD"), method = "render")
	public void onRenderPre(LivingEntityRenderState state, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i, CallbackInfo cbi) {
		cpm$renderPre(state, poseStack, multiBufferSource, i);
	}

	@Inject(at = @At("RETURN"), method = "render")
	public void onRenderPost(LivingEntityRenderState state, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i, CallbackInfo cbi) {
		cpm$renderPost(state, poseStack, multiBufferSource, i);
	}
}
