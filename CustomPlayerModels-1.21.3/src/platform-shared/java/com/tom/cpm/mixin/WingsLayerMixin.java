package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(WingsLayer.class)
public abstract class WingsLayerMixin extends RenderLayer<HumanoidRenderState, EntityModel<HumanoidRenderState>> {

	public WingsLayerMixin(
			RenderLayerParent<HumanoidRenderState, EntityModel<HumanoidRenderState>> renderLayerParent) {
		super(renderLayerParent);
	}

	private @Shadow @Final ElytraModel elytraModel;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"),
			method = "render")
	public void preRender(final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i,
			final HumanoidRenderState humanoidRenderState, final float f, final float g, CallbackInfo cbi) {
		if(getParentModel() instanceof HumanoidModel) {
			CustomPlayerModelsClient.INSTANCE.renderElytra((HumanoidModel<HumanoidRenderState>) getParentModel(), elytraModel);
		}
	}

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"),
			method = "render")
	public void postRender(final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i,
			final HumanoidRenderState humanoidRenderState, final float f, final float g, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbind(elytraModel);
	}
}
