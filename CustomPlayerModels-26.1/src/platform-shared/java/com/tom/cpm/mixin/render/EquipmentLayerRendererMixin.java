package com.tom.cpm.mixin.render;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;

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
			+ "Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;"
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;"
			+ "ILnet/minecraft/resources/Identifier;II)V";

	@Inject(at =
			@At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel("
							+ "Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
							+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
							+ "Lnet/minecraft/client/renderer/rendertype/RenderType;III"
							+ "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;I"
							+ "Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;"
							+ ")V",
							shift = Shift.BEFORE,
							ordinal = 2
					),
			method = CPMRENDERLAYERSMETHOD)
	private <S> void onSumbitArmorTrim(
			EquipmentClientInfo.LayerType layerType,
			ResourceKey<EquipmentAsset> p_387603_,
			Model<? super S> model,
			S p_435806_,
			ItemStack p_371670_,
			PoseStack p_371767_,
			SubmitNodeCollector p_435795_,
			int p_371309_,
			@Nullable Identifier p_371639_,
			int p_435821_,
			int p_436591_,
			CallbackInfo cbi
			) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(Sheets.ARMOR_TRIMS_SHEET, PlayerRenderManager.armor), layerType == LayerType.HUMANOID_LEGGINGS ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;armorCutoutNoCull(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;", shift = Shift.BEFORE), method = CPMRENDERLAYERSMETHOD)
	public void grabTexture(
			EquipmentClientInfo.LayerType layerType,
			ResourceKey<EquipmentAsset> p_387603_,
			Model<Object> model,
			Object p_435806_,
			ItemStack p_371670_,
			PoseStack p_371767_,
			SubmitNodeCollector p_435795_,
			int p_371309_,
			@Nullable Identifier p_371639_,
			int p_435821_,
			int p_436591_,
			CallbackInfo cbi,
			@Local(ordinal = 1) LocalRef<Identifier> Identifier2) {
		if (layerType == LayerType.HUMANOID || layerType == LayerType.HUMANOID_LEGGINGS) {
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(Identifier2.get(), PlayerRenderManager.armor), layerType == LayerType.HUMANOID_LEGGINGS ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
		} else if (layerType == LayerType.WINGS) {
			ModelTexture mt = new ModelTexture(Identifier2.get());
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, mt, TextureSheetType.ELYTRA);
			Identifier2.set(mt.getTexture());
		}
	}
}
