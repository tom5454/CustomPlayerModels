package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.optifine.player.PlayerItemModel;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(PlayerItemModel.class)
public class PlayerItemModelOFMixin {

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;"
					+ "getSkinTexture()Lnet/minecraft/util/Identifier;"
			), method =
		{
				"render(Lnet/minecraft/client/render/entity/model/BipedEntityModel;"
						+ "Lnet/minecraft/client/network/AbstractClientPlayerEntity;"
						+ "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
						"render(Lnet/minecraft/class_572;Lnet/minecraft/class_742;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;II)V"
		})
	public Identifier getPlayerSkin(AbstractClientPlayerEntity pe, BipedEntityModel modelBiped, AbstractClientPlayerEntity player, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, int packedOverlayIn) {
		CallbackInfoReturnable<Identifier> cbi = new CallbackInfoReturnable<>(null, true, player.getSkinTexture());
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(modelBiped, cbi);
		return cbi.getReturnValue();
	}
}
