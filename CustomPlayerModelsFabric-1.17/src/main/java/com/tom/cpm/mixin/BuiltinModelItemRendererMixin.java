package com.tom.cpm.mixin;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.SkullBlock.SkullType;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;

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
					locals = LocalCapture.CAPTURE_FAILHARD,
					require = 0)//Optifine
	public void onRender(ItemStack stack, ModelTransformation.Mode arg1, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int arg5, CallbackInfo ci, Item item, Block block, GameProfile gameProfile, SkullBlock.SkullType skullType, SkullBlockEntityModel model) {
		RefHolder.CPM_MODELS = skullModels;
		if(skullType == SkullBlock.Type.PLAYER && gameProfile != null) {
			CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfile, vertexConsumers);
		}
	}

	@Inject(at = @At("HEAD"),
			method = {"renderRaw(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;"
					+ "Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
					"renderRaw(Lnet/minecraft/class_1799;Lnet/minecraft/class_4587;"
							+ "Lnet/minecraft/class_4597;II)V"},
			require = 0,
			remap = false)//Optifine
	public void onRenderOF(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int arg5, CallbackInfo ci) {
		Item item = stack.getItem();
		if (item instanceof BlockItem) {
			Block block = ((BlockItem)item).getBlock();
			if (block instanceof AbstractSkullBlock) {
				GameProfile gameProfile = null;
				if (stack.hasTag()) {
					NbtCompound nbtCompound = stack.getTag();
					if (nbtCompound.contains("SkullOwner", 10)) {
						gameProfile = NbtHelper.toGameProfile(nbtCompound.getCompound("SkullOwner"));
					} else if (nbtCompound.contains("SkullOwner", 8)
							&& !StringUtils.isBlank(nbtCompound.getString("SkullOwner"))) {
						gameProfile = new GameProfile((UUID) null, nbtCompound.getString("SkullOwner"));
						nbtCompound.remove("SkullOwner");
						SkullBlockEntity.loadProperties(gameProfile, p_172558_1_ -> nbtCompound.put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), p_172558_1_)));
					}
				}
				SkullType skullType = ((AbstractSkullBlock)block).getSkullType();
				SkullBlockEntityModel skullmodelbase = this.skullModels.get(skullType);
				RefHolder.CPM_MODELS = skullModels;
				if(skullType == SkullBlock.Type.PLAYER && gameProfile != null) {
					CustomPlayerModelsClient.INSTANCE.renderSkull(skullmodelbase, gameProfile, vertexConsumers);
				}
			}
		}
	}
}
