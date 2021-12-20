package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(ElytraLayer.class)
public class ElytraLayerMixin {
	private @Shadow @Final ElytraModel<LivingEntity> elytraModel;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"),
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
					+ "Lnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
	public void preRender(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(entitylivingbaseIn instanceof AbstractClientPlayer)ClientProxy.INSTANCE.renderElytra((Player) entitylivingbaseIn, bufferIn, elytraModel);
	}

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"),
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
					+ "Lnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
	public void postRender(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(entitylivingbaseIn instanceof AbstractClientPlayer)ClientProxy.INSTANCE.unbind(elytraModel);
	}

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull("
					+ "Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",
					ordinal = 0
			),
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
					+ "Lnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
	private RenderType onGetRenderType(ResourceLocation resLoc, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if(entitylivingbaseIn instanceof AbstractClientPlayer) {
			ModelTexture mt = new ModelTexture(resLoc, PlayerRenderManager.armor);
			ClientProxy.mc.getPlayerRenderManager().bindSkin(elytraModel, mt, TextureSheetType.ELYTRA);
			return mt.getRenderType();
		}
		return RenderType.armorCutoutNoCull(resLoc);
	}
}
