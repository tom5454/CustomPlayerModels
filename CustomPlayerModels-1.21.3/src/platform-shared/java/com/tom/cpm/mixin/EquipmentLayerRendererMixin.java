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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.item.equipment.EquipmentModel.LayerType;
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
			+ "Lnet/minecraft/world/item/equipment/EquipmentModel$LayerType;"
			+ "Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/Model;"
			+ "Lnet/minecraft/world/item/ItemStack;"
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;"
			+ "ILnet/minecraft/resources/ResourceLocation;)V";

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;", shift = Shift.BEFORE), method = CPMRENDERLAYERSMETHOD)
	public void grabTexture(EquipmentModel.LayerType layerType,
			ResourceLocation resourceLocation,
			Model model,
			ItemStack itemStack,
			PoseStack poseStack,
			MultiBufferSource multiBufferSource,
			int i,
			@Nullable ResourceLocation resourceLocation2,
			CallbackInfo cbi,
			@Local(ordinal = 2) LocalRef<ResourceLocation> resourceLocation3) {
		if (layerType == LayerType.HUMANOID || layerType == LayerType.HUMANOID_LEGGINGS) {
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(resourceLocation3.get(), PlayerRenderManager.armor), layerType == LayerType.HUMANOID_LEGGINGS ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
		} else if (layerType == LayerType.WINGS) {
			ModelTexture mt = new ModelTexture(resourceLocation3.get());
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, mt, TextureSheetType.ELYTRA);
			resourceLocation3.set(mt.getTexture());
		}
	}

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/model/Model;renderToBuffer("
							+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
					),
			method = CPMRENDERLAYERSMETHOD)
	public void grabTexture(EquipmentModel.LayerType layerType,
			ResourceLocation resourceLocation,
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
