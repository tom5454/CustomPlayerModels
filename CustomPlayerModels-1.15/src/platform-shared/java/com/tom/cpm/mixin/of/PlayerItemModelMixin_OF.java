package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;
import net.optifine.player.PlayerItemModel;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(PlayerItemModel.class)
public class PlayerItemModelMixin_OF {

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;"
					+ "getSkinTextureLocation()Lnet/minecraft/util/ResourceLocation;"
			), method = "render(Lnet/minecraft/client/renderer/entity/model/BipedModel;"
					+ "Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;"
					+ "Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;II)V")
	public ResourceLocation getPlayerSkin(AbstractClientPlayerEntity pe, BipedModel modelBiped, AbstractClientPlayerEntity player, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, int packedOverlayIn) {
		ModelTexture mt = new ModelTexture(player.getSkinTextureLocation());
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(modelBiped, mt, TextureSheetType.SKIN);
		return mt.getTexture();
	}
}
