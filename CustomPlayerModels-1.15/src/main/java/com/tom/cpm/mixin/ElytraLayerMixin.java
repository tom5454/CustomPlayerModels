package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(ElytraLayer.class)
public class ElytraLayerMixin {
	private @Shadow @Final ElytraModel<LivingEntity> modelElytra;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/matrix/MatrixStack;push()V"),
			method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void preRender(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(entitylivingbaseIn instanceof AbstractClientPlayerEntity)ClientProxy.INSTANCE.renderElytra((PlayerEntity) entitylivingbaseIn, bufferIn, modelElytra);
	}

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/matrix/MatrixStack;pop()V"),
			method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void postRender(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(entitylivingbaseIn instanceof AbstractClientPlayerEntity)ClientProxy.INSTANCE.unbind(modelElytra);
	}

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/model/ElytraModel;getRenderType("
					+ "Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",
					ordinal = 0
			),
			method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	private RenderType onGetRenderTypeNoSkin(ElytraModel<LivingEntity> modelElytra, ResourceLocation resLoc, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if(entitylivingbaseIn instanceof AbstractClientPlayerEntity) {
			CallbackInfoReturnable<ResourceLocation> cbi = new CallbackInfoReturnable<>(null, true, resLoc);
			ClientProxy.mc.getPlayerRenderManager().bindSkin(modelElytra, cbi, TextureSheetType.ELYTRA);
			return modelElytra.getRenderType(cbi.getReturnValue());
		}
		return modelElytra.getRenderType(resLoc);
	}
}
