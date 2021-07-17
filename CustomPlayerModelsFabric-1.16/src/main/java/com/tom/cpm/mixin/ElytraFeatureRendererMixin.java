package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(ElytraFeatureRenderer.class)
public class ElytraFeatureRendererMixin {
	private @Shadow @Final ElytraEntityModel<LivingEntity> elytra;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"),
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void preRender(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(entitylivingbaseIn instanceof AbstractClientPlayerEntity)CustomPlayerModelsClient.INSTANCE.renderElytra((PlayerEntity) entitylivingbaseIn, bufferIn, elytra);
	}

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"),
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void postRender(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(entitylivingbaseIn instanceof AbstractClientPlayerEntity)CustomPlayerModelsClient.INSTANCE.unbind(elytra);
	}

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/RenderLayer;getArmorCutoutNoCull("
					+ "Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
					ordinal = 0
			),
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	private RenderLayer onGetRenderTypeNoSkin(Identifier resLoc, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if(entitylivingbaseIn instanceof AbstractClientPlayerEntity) {
			CallbackInfoReturnable<Identifier> cbi = new CallbackInfoReturnable<>(null, true, resLoc);
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(elytra, cbi, TextureSheetType.ELYTRA);
			return RenderLayer.getArmorCutoutNoCull(cbi.getReturnValue());
		}
		return RenderLayer.getArmorCutoutNoCull(resLoc);
	}
}
