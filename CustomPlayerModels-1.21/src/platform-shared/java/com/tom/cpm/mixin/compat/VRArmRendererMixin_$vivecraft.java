package com.tom.cpm.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.VRArmRenderer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(VRArmRenderer.class)
public class VRArmRendererMixin_$vivecraft extends PlayerRenderer {

	public VRArmRendererMixin_$vivecraft(Context pContext, boolean pUseSlimModel) {
		super(pContext, pUseSlimModel);
	}

	@Inject(at = @At("HEAD"), method = "renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V")
	public void onRenderRightArmPre(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.bindHand(player, vertexConsumers, getModel());
	}

	@Inject(at = @At("HEAD"), method = "renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V")
	public void onRenderLeftArmPre(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.bindHand(player, vertexConsumers, getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V")
	public void onRenderRightArmPost(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V")
	public void onRenderLeftArmPost(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(getModel());
	}

	@Redirect(at =
			@At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/resources/PlayerSkin;"
							+ "texture()Lnet/minecraft/resources/ResourceLocation;"
					),
			method = "renderItem("
					+ "Lorg/vivecraft/client_vr/provider/ControllerType;"
					+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;"
					+ "ILnet/minecraft/client/player/AbstractClientPlayer;"
					+ "Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;)V"
			)
	public ResourceLocation getSkinTex(PlayerSkin skin, ControllerType side, PoseStack matrixStackIn, MultiBufferSource bufferIn,
			int combinedLightIn, AbstractClientPlayer player, ModelPart rendererArmIn, ModelPart rendererArmwearIn) {
		return getTextureLocation(player);
	}
}
