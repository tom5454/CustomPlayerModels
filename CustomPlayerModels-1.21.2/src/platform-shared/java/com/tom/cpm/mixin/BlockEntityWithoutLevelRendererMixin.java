package com.tom.cpm.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.RefHolder;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BlockEntityWithoutLevelRendererMixin {
	private @Shadow Map<SkullBlock.Type, SkullModelBase> skullModels;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;"
					+ "getRenderType(Lnet/minecraft/world/level/block/SkullBlock$Type;Lnet/minecraft/world/item/component/ResolvableProfile;)"
					+ "Lnet/minecraft/client/renderer/RenderType;",
					remap = true
			),
			method = "renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;"
					+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
					locals = LocalCapture.CAPTURE_FAILHARD,
					require = 0)//Optifine
	@Surrogate
	public void onRender(ItemStack stack, ItemDisplayContext arg1, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int arg5, CallbackInfo ci, Item item, Block block, AbstractSkullBlock abstractSkullBlock, ResolvableProfile gameProfile, SkullModelBase model) {
		RefHolder.CPM_MODELS = skullModels;
		if(abstractSkullBlock.getType() == SkullBlock.Types.PLAYER && gameProfile != null) {
			CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfile.gameProfile(), vertexConsumers);
		}
	}

	@Surrogate
	public void onRender(ItemStack stack, ItemDisplayContext arg1, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int arg5, CallbackInfo ci, Item item, Block block, AbstractSkullBlock abstractSkullBlock, ResolvableProfile gameProfile) {
		RefHolder.CPM_MODELS = skullModels;
		if(abstractSkullBlock.getType() == SkullBlock.Types.PLAYER && gameProfile != null) {
			SkullModelBase model = this.skullModels.get(abstractSkullBlock.getType());
			CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfile.gameProfile(), vertexConsumers);
		}
	}
}
