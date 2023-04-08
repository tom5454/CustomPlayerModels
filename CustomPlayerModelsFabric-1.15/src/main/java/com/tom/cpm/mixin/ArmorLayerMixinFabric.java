package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArmorLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(ArmorLayer.class)
public abstract class ArmorLayerMixinFabric extends LayerRenderer<LivingEntity, BipedModel<LivingEntity>> {

	public ArmorLayerMixinFabric(IEntityRenderer<LivingEntity, BipedModel<LivingEntity>> p_i50926_1_) {
		super(p_i50926_1_);
	}

	private @Final @Shadow BipedModel<LivingEntity> innerModel;
	private @Final @Shadow BipedModel<LivingEntity> outerModel;
	@Shadow abstract ResourceLocation getArmorLocation(ArmorItem armorItem, boolean bl, String string);

	@Inject(at = @At("HEAD"), method = "renderModel(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
			+ "Lnet/minecraft/item/ArmorItem;ZLnet/minecraft/client/renderer/entity/model/BipedModel;ZFFFLjava/lang/String;)V")
	private void preRenderTexture(MatrixStack p_117107_, IRenderTypeBuffer p_117108_, int p_117109_, ArmorItem p_117110_, boolean p_117111_, BipedModel<LivingEntity> model, boolean p_117113_, float p_117114_, float p_117115_, float p_117116_, String p_117117_, CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(getArmorLocation(p_117110_, p_117113_, p_117117_), PlayerRenderManager.armor), model == innerModel ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
	}

	@Inject(at = @At("HEAD"), target = @Desc(value = "renderArmor", args = {MatrixStack.class, IRenderTypeBuffer.class, int.class, boolean.class, BipedModel.class, float.class, float.class, float.class, ResourceLocation.class}), remap = false, require = 0, expect = 0)
	private void preRenderTexture(MatrixStack p_117107_, IRenderTypeBuffer p_117108_, int p_117109_, boolean p_117111_, BipedModel<LivingEntity> model, float p_117114_, float p_117115_, float p_117116_, ResourceLocation resLoc, CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(resLoc, PlayerRenderManager.armor), model == innerModel ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
	}
}
