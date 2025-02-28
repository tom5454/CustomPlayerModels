package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin extends RenderLayer<HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

	public HumanoidArmorLayerMixin(
			RenderLayerParent<HumanoidRenderState, HumanoidModel<HumanoidRenderState>> renderLayerParent) {
		super(renderLayerParent);
	}

	private @Final @Shadow HumanoidModel<HumanoidRenderState> innerModel;
	private @Final @Shadow HumanoidModel<HumanoidRenderState> outerModel;

	@Inject(at = @At("HEAD"), method = "render")
	public void preRender(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, HumanoidRenderState entitylivingbaseIn, float a, float b, CallbackInfo cbi) {
		if(getParentModel() instanceof HumanoidModel) {
			CustomPlayerModelsClient.INSTANCE.renderArmor(outerModel, innerModel, getParentModel());
		}
	}

	@Inject(at = @At("RETURN"), method = "render")
	public void postRender(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, HumanoidRenderState entitylivingbaseIn, float a, float b, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbind(outerModel);
		CustomPlayerModelsClient.INSTANCE.manager.unbind(innerModel);
	}
}
