package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(value = LivingEntityRenderer.class, priority = 100000)
public abstract class LivingEntityRendererFabric<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
extends EntityRenderer<T, S>
implements RenderLayerParent<S, M>, com.tom.cpm.client.EntityRenderer<S> {

	protected LivingEntityRendererFabric(Context context) {
		super(context);
	}

	@Inject(at = @At("HEAD"), method = "submit("
			+ "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;"
			+ "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
			+ "Lnet/minecraft/client/renderer/state/CameraRenderState;"
			+ ")V")
	public void onSubmitPre(S livingEntityRenderState, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState,
			CallbackInfo cbi, @Local LocalRef<SubmitNodeCollector> snc) {
		cpm$onSubmitPre(livingEntityRenderState, poseStack, submitNodeCollector, cameraRenderState, snc);
	}

	@Inject(at = @At("RETURN"), method = "submit("
			+ "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;"
			+ "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
			+ "Lnet/minecraft/client/renderer/state/CameraRenderState;"
			+ ")V")
	public void onSubmitPost(S livingEntityRenderState, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState,
			CallbackInfo cbi) {
		cpm$onSubmitPost(livingEntityRenderState, poseStack, submitNodeCollector, cameraRenderState);
	}
}
