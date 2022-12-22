package com.tom.cpm.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin extends RenderLayer<LivingEntity, HumanoidModel<LivingEntity>> {

	public HumanoidArmorLayerMixin(RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> pRenderer) {
		super(pRenderer);
	}

	private @Final @Shadow HumanoidModel<LivingEntity> innerModel;
	private @Final @Shadow HumanoidModel<LivingEntity> outerModel;
	@Shadow abstract ResourceLocation getArmorLocation(ArmorItem armorItem, boolean bl, @Nullable String string);

	@Inject(at = @At("HEAD"),
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
					+ "Lnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
	public void preRender(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(getParentModel() instanceof HumanoidModel) {
			CustomPlayerModelsClient.INSTANCE.renderArmor(outerModel, innerModel, getParentModel());
		}
	}

	@Inject(at = @At("RETURN"),
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
					+ "Lnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
	public void postRender(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbind(outerModel);
		CustomPlayerModelsClient.INSTANCE.manager.unbind(innerModel);
	}

	@Inject(at = @At("HEAD"), method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
			+ "Lnet/minecraft/world/item/ArmorItem;ZLnet/minecraft/client/model/HumanoidModel;ZFFFLjava/lang/String;)V")
	private void preRenderTexture(PoseStack p_117107_, MultiBufferSource p_117108_, int p_117109_, ArmorItem p_117110_, boolean p_117111_, HumanoidModel<LivingEntity> model, boolean p_117113_, float p_117114_, float p_117115_, float p_117116_, @Nullable String p_117117_, CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(getArmorLocation(p_117110_, p_117113_, p_117117_), PlayerRenderManager.armor), model == innerModel ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
	}

	@Inject(at = @At("HEAD"), target = @Desc(value = "renderModel", args = {PoseStack.class, MultiBufferSource.class, int.class, boolean.class, Model.class, float.class, float.class, float.class, ResourceLocation.class}), remap = false, require = 0, expect = 0)
	private void preRenderTexture(PoseStack p_117107_, MultiBufferSource p_117108_, int p_117109_, boolean p_117111_, Model model, float p_117114_, float p_117115_, float p_117116_, ResourceLocation resLoc, CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(resLoc, PlayerRenderManager.armor), model == innerModel ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
	}
}
