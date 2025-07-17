package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixinNeo extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {

	public PlayerRendererMixinNeo(Context p_174289_, PlayerModel p_174290_, float p_174291_) {
		super(p_174289_, p_174290_, p_174291_);
	}

	@Inject(at = @At("HEAD"), target = @Desc(value = "renderRightHand", args = {PoseStack.class, MultiBufferSource.class, int.class, ResourceLocation.class, boolean.class, AbstractClientPlayer.class}))
	public void onRenderRightArmPre(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, AbstractClientPlayer pl, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHand(pl, vertexConsumers, getModel());
	}

	@Inject(at = @At("HEAD"), target = @Desc(value = "renderLeftHand", args = {PoseStack.class, MultiBufferSource.class, int.class, ResourceLocation.class, boolean.class, AbstractClientPlayer.class}))
	public void onRenderLeftArmPre(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, AbstractClientPlayer pl, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHand(pl, vertexConsumers, getModel());
	}

	@Inject(at = @At("RETURN"), target = @Desc(value = "renderRightHand", args = {PoseStack.class, MultiBufferSource.class, int.class, ResourceLocation.class, boolean.class, AbstractClientPlayer.class}))
	public void onRenderRightArmPost(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, AbstractClientPlayer pl, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(vertexConsumers, getModel());
	}

	@Inject(at = @At("RETURN"), target = @Desc(value = "renderLeftHand", args = {PoseStack.class, MultiBufferSource.class, int.class, ResourceLocation.class, boolean.class, AbstractClientPlayer.class}))
	public void onRenderLeftArmPost(final PoseStack poseStack, final MultiBufferSource vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, AbstractClientPlayer pl, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(vertexConsumers, getModel());
	}
}
