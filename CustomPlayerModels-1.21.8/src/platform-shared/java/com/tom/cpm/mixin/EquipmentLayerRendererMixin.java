package com.tom.cpm.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {
	private static final String CPMRENDERLAYERSMETHOD = "renderLayers("
			+ "Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;"
			+ "Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;"
			+ "Lnet/minecraft/world/item/ItemStack;"
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;"
			+ "ILnet/minecraft/resources/ResourceLocation;)V";

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;", shift = Shift.BEFORE), method = CPMRENDERLAYERSMETHOD)
	public void grabTexture(EquipmentClientInfo.LayerType layerType,
			ResourceKey<EquipmentAsset> resourceKey,
			Model model,
			ItemStack itemStack,
			PoseStack poseStack,
			MultiBufferSource multiBufferSource,
			int i,
			@Nullable ResourceLocation resourceLocation,
			CallbackInfo cbi,
			@Local(ordinal = 1) LocalRef<ResourceLocation> resourceLocation2) {
		if (layerType == LayerType.HUMANOID || layerType == LayerType.HUMANOID_LEGGINGS) {
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(resourceLocation2.get(), PlayerRenderManager.armor), layerType == LayerType.HUMANOID_LEGGINGS ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
		} else if (layerType == LayerType.WINGS) {
			ModelTexture mt = new ModelTexture(resourceLocation2.get());
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, mt, TextureSheetType.ELYTRA);
			resourceLocation2.set(mt.getTexture());
		}
	}

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/model/Model;renderToBuffer("
							+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
					),
			method = CPMRENDERLAYERSMETHOD)
	public void grabTexture(EquipmentClientInfo.LayerType layerType,
			ResourceKey<EquipmentAsset> resourceKey,
			Model model,
			ItemStack itemStack,
			PoseStack poseStack,
			MultiBufferSource multiBufferSource,
			int i,
			@Nullable ResourceLocation resourceLocation2,
			CallbackInfo cbi,
			@Local ArmorTrim armorTrim) {
		if (layerType == LayerType.HUMANOID || layerType == LayerType.HUMANOID_LEGGINGS) {
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(Sheets.ARMOR_TRIMS_SHEET, PlayerRenderManager.armor), layerType == LayerType.HUMANOID_LEGGINGS ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
		}
	}
}
