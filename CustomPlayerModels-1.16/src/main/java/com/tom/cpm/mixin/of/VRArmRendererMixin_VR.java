package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.render.VRArmRenderer;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.tom.cpm.client.ClientProxy;

@Mixin(VRArmRenderer.class)
public class VRArmRendererMixin_VR extends PlayerRenderer {

	public VRArmRendererMixin_VR(EntityRendererManager p_i46102_1_) {
		super(p_i46102_1_);
	}

	@Inject(at = @At("HEAD"), method = {
			"renderRightHand(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V",
			"func_229144_a_(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V"
	}, remap = false)
	public void onRenderRightArmPre(MatrixStack matrices, IRenderTypeBuffer vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		ClientProxy.INSTANCE.manager.bindHand(player, vertexConsumers, getModel());
	}

	@Inject(at = @At("HEAD"), method = {
			"renderLeftHand(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V",
			"func_229146_b_(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V"
	}, remap = false)
	public void onRenderLeftArmPre(MatrixStack matrices, IRenderTypeBuffer vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		ClientProxy.INSTANCE.manager.bindHand(player, vertexConsumers, getModel());
	}

	@Inject(at = @At("RETURN"), method = {
			"renderRightHand(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V",
			"func_229144_a_(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V"
	}, remap = false)
	public void onRenderRightArmPost(MatrixStack matrices, IRenderTypeBuffer vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		ClientProxy.INSTANCE.unbind(getModel());
	}

	@Inject(at = @At("RETURN"), method = {
			"renderLeftHand(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V",
			"func_229146_b_(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V"
	}, remap = false)
	public void onRenderLeftArmPost(MatrixStack matrices, IRenderTypeBuffer vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		ClientProxy.INSTANCE.unbind(getModel());
	}

	@Redirect(at =
			@At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;"
							+ "getSkinTextureLocation()Lnet/minecraft/util/ResourceLocation;",
							remap = true
					),
			method = "renderItem("
					+ "Lorg/vivecraft/control/ControllerType;"
					+ "Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;"
					+ "ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;"
					+ "Lnet/minecraft/client/renderer/model/ModelRenderer;Lnet/minecraft/client/renderer/model/ModelRenderer;)V",
					remap = false
			)
	public ResourceLocation getSkinTex(AbstractClientPlayerEntity player) {
		return getTextureLocation(player);
	}
}
