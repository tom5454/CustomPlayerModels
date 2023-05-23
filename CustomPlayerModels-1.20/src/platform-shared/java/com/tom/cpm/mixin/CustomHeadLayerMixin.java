package com.tom.cpm.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.RefHolder;

@Mixin(CustomHeadLayer.class)
public class CustomHeadLayerMixin {
	private @Shadow @Final Map<SkullBlock.Type, SkullModelBase> skullModels;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;"
					+ "getRenderType(Lnet/minecraft/world/level/block/SkullBlock$Type;Lcom/mojang/authlib/GameProfile;)"
					+ "Lnet/minecraft/client/renderer/RenderType;"
			),
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
					+ "Lnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
	public void onRender(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
		ItemStack itemstack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
		Item item = itemstack.getItem();
		SkullBlock.Type skullType = ((AbstractSkullBlock)((BlockItem)item).getBlock()).getType();
		if(skullType == SkullBlock.Types.PLAYER) {
			RefHolder.CPM_MODELS = skullModels;
			GameProfile gameProfile = null;
			if (itemstack.hasTag()) {
				CompoundTag compoundtag = itemstack.getTag();
				if (compoundtag.contains("SkullOwner", 10)) {
					gameProfile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
				}
			}
			if(gameProfile != null) {
				SkullModelBase model = this.skullModels.get(skullType);
				CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfile, vertexConsumerProvider);
			}
		}
	}
}
