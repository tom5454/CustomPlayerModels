package com.tom.cpm.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.render.VRArmRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(VRArmRenderer.class)
public class VRArmRendererMixin_$vivecraft extends PlayerRenderer {

	public VRArmRendererMixin_$vivecraft(Context pContext, boolean pUseSlimModel) {
		super(pContext, pUseSlimModel);
	}

	@Inject(at = @At("HEAD"), method = "renderRightHand")
	public void onRenderRightArmPre(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHand(vertexConsumers, getModel());
	}

	@Inject(at = @At("HEAD"), method = "renderLeftHand")
	public void onRenderLeftArmPre(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHand(vertexConsumers, getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderRightHand")
	public void onRenderRightArmPost(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(vertexConsumers, getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderLeftHand")
	public void onRenderLeftArmPost(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(vertexConsumers, getModel());
	}

	@ModifyArg(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent("
					+ "Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
			), method = "renderItem")
	public ResourceLocation getSkinTex(ResourceLocation arg) {
		ModelTexture tex = new ModelTexture(arg);
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(getModel(), tex, TextureSheetType.SKIN);
		return tex.getTexture();
	}
}
