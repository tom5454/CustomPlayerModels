package com.tom.cpm.mixin;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {
	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1, shift = Shift.BEFORE), target = @Desc(value = "renderLayers", args = {
			EquipmentModel.LayerType.class,
			ResourceLocation.class,
			Model.class,
			ItemStack.class,
			Function.class,
			PoseStack.class,
			MultiBufferSource.class,
			int.class,
			ResourceLocation.class
	}))
	public void grabTexture(EquipmentModel.LayerType layerType,
			ResourceLocation resourceLocation,
			Model model,
			ItemStack itemStack,
			Function<ResourceLocation, RenderType> function,
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
					desc = @Desc(owner = Model.class, value = "renderToBuffer",
					args = {
							PoseStack.class,
							VertexConsumer.class,
							int.class,
							int.class
					})),
			target = @Desc(value = "renderLayers", args = {
					EquipmentModel.LayerType.class,
					ResourceLocation.class,
					Model.class,
					ItemStack.class,
					Function.class,
					PoseStack.class,
					MultiBufferSource.class,
					int.class,
					ResourceLocation.class
			}), locals = LocalCapture.CAPTURE_FAILHARD)
	public void grabTexture(EquipmentModel.LayerType layerType,
			ResourceLocation resourceLocation,
			Model model,
			ItemStack itemStack,
			Function<ResourceLocation, RenderType> function,
			PoseStack poseStack,
			MultiBufferSource multiBufferSource,
			int i,
			@Nullable ResourceLocation resourceLocation2,
			CallbackInfo cbi,
			ArmorTrim armorTrim) {
		if (layerType == LayerType.HUMANOID || layerType == LayerType.HUMANOID_LEGGINGS) {
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(Sheets.ARMOR_TRIMS_SHEET, PlayerRenderManager.armor), layerType == LayerType.HUMANOID_LEGGINGS ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
		}
	}
}
