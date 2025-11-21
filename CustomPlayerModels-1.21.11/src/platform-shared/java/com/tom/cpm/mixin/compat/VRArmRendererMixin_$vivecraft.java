package com.tom.cpm.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.render.VRArmRenderer;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CPMOrderedSubmitNodeCollector.CPMSubmitNodeCollector;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(VRArmRenderer.class)
public class VRArmRendererMixin_$vivecraft extends AvatarRenderer<AbstractClientPlayer> {

	public VRArmRendererMixin_$vivecraft(Context p_445612_, boolean p_445726_) {
		super(p_445612_, p_445726_);
	}

	@Inject(at = @At("HEAD"), method = "renderRightHand")
	public void onRenderRightArmPre(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final Identifier Identifier, final boolean sleeve, CallbackInfo cbi, @Local LocalRef<SubmitNodeCollector> snc) {
		CPMSubmitNodeCollector.injectSNC(snc);
		CustomPlayerModelsClient.INSTANCE.renderHand(getModel());
	}

	@Inject(at = @At("HEAD"), method = "renderLeftHand")
	public void onRenderLeftArmPre(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final Identifier Identifier, final boolean sleeve, CallbackInfo cbi, @Local LocalRef<SubmitNodeCollector> snc) {
		CPMSubmitNodeCollector.injectSNC(snc);
		CustomPlayerModelsClient.INSTANCE.renderHand(getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderRightHand")
	public void onRenderRightArmPost(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final Identifier Identifier, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderLeftHand")
	public void onRenderLeftArmPost(final PoseStack poseStack, final SubmitNodeCollector vertexConsumers, final int i, final Identifier Identifier, final boolean sleeve, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHandPost(getModel());
	}

	@ModifyArg(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/rendertype/RenderType;entityTranslucent("
					+ "Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
			), method = "renderHand")
	public Identifier getSkinTex(Identifier arg) {
		ModelTexture tex = new ModelTexture(arg);
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(getModel(), tex, TextureSheetType.SKIN);
		return tex.getTexture();
	}
}
