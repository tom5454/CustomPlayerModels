package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.ResourceLocation;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CPMOrderedSubmitNodeCollector.CPMSubmitNodeCollector;
import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixinNeo extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {

	public AvatarRendererMixinNeo(Context p_174289_, PlayerModel p_174290_, float p_174291_) {
		super(p_174289_, p_174290_, p_174291_);
	}

	@Inject(at = @At("HEAD"), target = @Desc(value = "submit", args = {AvatarRenderState.class, PoseStack.class, SubmitNodeCollector.class, CameraRenderState.class}))
	public void onSubmit(AvatarRenderState p_433493_, PoseStack p_434615_, SubmitNodeCollector p_433768_, CameraRenderState p_450931_, CallbackInfo cbi, @Local LocalRef<SubmitNodeCollector> snc) {
		snc.set(new CPMSubmitNodeCollector(p_433768_));
	}

	@Inject(at = @At("HEAD"), target = @Desc(value = "renderRightHand", args = {PoseStack.class, SubmitNodeCollector.class, int.class, ResourceLocation.class, boolean.class, AbstractClientPlayer.class}))
	public void onRenderRightArmPre(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, AbstractClientPlayer pl, CallbackInfo cbi, @Local LocalRef<SubmitNodeCollector> snc) {
		snc.set(new CPMSubmitNodeCollector(vertexConsumers));
		CustomPlayerModelsClient.INSTANCE.renderHand(pl, getModel());
	}

	@Inject(at = @At("HEAD"), target = @Desc(value = "renderLeftHand", args = {PoseStack.class, SubmitNodeCollector.class, int.class, ResourceLocation.class, boolean.class, AbstractClientPlayer.class}))
	public void onRenderLeftArmPre(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, AbstractClientPlayer pl, CallbackInfo cbi, @Local LocalRef<SubmitNodeCollector> snc) {
		snc.set(new CPMSubmitNodeCollector(vertexConsumers));
		CustomPlayerModelsClient.INSTANCE.renderHand(pl, getModel());
	}

	@Inject(at = @At("RETURN"), target = @Desc(value = "renderRightHand", args = {PoseStack.class, SubmitNodeCollector.class, int.class, ResourceLocation.class, boolean.class, AbstractClientPlayer.class}))
	public void onRenderRightArmPost(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, AbstractClientPlayer pl, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(getModel());
	}

	@Inject(at = @At("RETURN"), target = @Desc(value = "renderLeftHand", args = {PoseStack.class, SubmitNodeCollector.class, int.class, ResourceLocation.class, boolean.class, AbstractClientPlayer.class}))
	public void onRenderLeftArmPost(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final ResourceLocation resourceLocation, final boolean sleeve, AbstractClientPlayer pl, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(getModel());
	}
}
