package com.tom.cpm.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.RefHolder;

@Mixin(HeadFeatureRenderer.class)
public class HeadFeatureRendererMixin {
	@Shadow private @Final Map<SkullBlock.SkullType, SkullBlockEntityModel> headModels;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/block/entity/SkullBlockEntityRenderer;"
					+ "getRenderLayer(Lnet/minecraft/block/SkullBlock$SkullType;Lcom/mojang/authlib/GameProfile;)"
					+ "Lnet/minecraft/client/render/RenderLayer;"
			),
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V",
					locals = LocalCapture.CAPTURE_FAILHARD)
	public void onRender(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci, ItemStack stack, Item item, GameProfile gameProfile, SkullBlock.SkullType skullType, SkullBlockEntityModel model) {
		RefHolder.CPM_MODELS = headModels;
		if(skullType == SkullBlock.Type.PLAYER && gameProfile != null) {
			CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfile, vertexConsumerProvider);
		}
	}
}
