package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.render.VRArmRenderer;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(VRArmRenderer.class)
public class VRArmRendererMixin_VR extends PlayerEntityRenderer {

	public VRArmRendererMixin_VR(Context pContext, boolean pUseSlimModel) {
		super(pContext, pUseSlimModel);
	}

	@Inject(at = @At("HEAD"), method = {
			"renderRightArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V",
			"method_4220(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_742;)V"
	}, remap = false)
	public void onRenderRightArmPre(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = true;
		CustomPlayerModelsClient.INSTANCE.manager.bindHand(player, vertexConsumers, getModel());
	}

	@Inject(at = @At("HEAD"), method = {
			"renderLeftArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V",
			"method_4221(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_742;)V"
	}, remap = false)
	public void onRenderLeftArmPre(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = true;
		CustomPlayerModelsClient.INSTANCE.manager.bindHand(player, vertexConsumers, getModel());
	}

	@Inject(at = @At("RETURN"), method = {
			"renderRightArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V",
			"method_4220(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_742;)V"
	}, remap = false)
	public void onRenderRightArmPost(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(getModel());
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = false;
	}

	@Inject(at = @At("RETURN"), method = {
			"renderLeftArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V",
			"method_4221(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_742;)V"
	}, remap = false)
	public void onRenderLeftArmPost(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(getModel());
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = false;
	}

	@Redirect(at =
			@At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;"
							+ "getSkinTexture()Lnet/minecraft/util/Identifier;"
					),
			method = "renderItem("
					+ "Lorg/vivecraft/provider/ControllerType;"
					+ "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;"
					+ "ILnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/model/ModelPart;"
					+ "Lnet/minecraft/client/model/ModelPart;)V"
			)
	public Identifier getSkinTex(AbstractClientPlayerEntity player) {
		return getTexture(player);
	}
}
