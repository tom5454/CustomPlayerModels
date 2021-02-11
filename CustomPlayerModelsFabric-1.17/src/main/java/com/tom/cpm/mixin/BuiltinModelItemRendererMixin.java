package com.tom.cpm.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.Block;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.RefHolder;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {
	@Shadow private Map<SkullBlock.SkullType, SkullBlockEntityModel> skullModels;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/block/entity/SkullBlockEntityRenderer;"
					+ "getRenderLayer(Lnet/minecraft/block/SkullBlock$SkullType;Lcom/mojang/authlib/GameProfile;)"
					+ "Lnet/minecraft/client/render/RenderLayer;"
			),
			method = "render(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;"
					+ "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
					locals = LocalCapture.CAPTURE_FAILHARD)
	public void onRender(ItemStack stack, ModelTransformation.Mode arg1, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int arg5, CallbackInfo ci, Item item, Block block, GameProfile gameProfile, SkullBlock.SkullType skullType, SkullBlockEntityModel model) {
		RefHolder.CPM_MODELS = skullModels;
		if(skullType == SkullBlock.Type.PLAYER && gameProfile != null) {
			CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfile, vertexConsumers);
		}
	}
}
