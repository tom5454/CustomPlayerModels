package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin extends LayerRenderer<LivingEntity, EntityModel<LivingEntity>> {
	public ElytraLayerMixin(IEntityRenderer<LivingEntity, EntityModel<LivingEntity>> p_i50926_1_) {
		super(p_i50926_1_);
	}

	private @Shadow @Final ElytraModel<LivingEntity> elytraModel;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/platform/GlStateManager;pushMatrix()V"),
			method = "render(Lnet/minecraft/entity/LivingEntity;FFFFFFF)V")
	public void preRender(LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo cbi) {
		if(getParentModel() instanceof BipedModel) {
			CustomPlayerModelsClient.INSTANCE.renderElytra((BipedModel<LivingEntity>) getParentModel(), elytraModel);
		}
	}

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/platform/GlStateManager;popMatrix()V"),
			method = "render(Lnet/minecraft/entity/LivingEntity;FFFFFFF)V")
	public void postRender(LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbind(elytraModel);
	}
}
