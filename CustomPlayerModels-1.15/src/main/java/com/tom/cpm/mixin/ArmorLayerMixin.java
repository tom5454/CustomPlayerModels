package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArmorLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(ArmorLayer.class)
public abstract class ArmorLayerMixin extends LayerRenderer<LivingEntity, BipedModel<LivingEntity>> {

	public ArmorLayerMixin(IEntityRenderer<LivingEntity, BipedModel<LivingEntity>> p_i50926_1_) {
		super(p_i50926_1_);
	}

	private @Final @Shadow BipedModel<LivingEntity> modelLeggings;
	private @Final @Shadow BipedModel<LivingEntity> modelArmor;

	@Inject(at = @At("HEAD"),
			method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void preRender(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(entitylivingbaseIn instanceof AbstractClientPlayerEntity) {
			ClientProxy.INSTANCE.renderArmor(modelArmor, modelLeggings, (PlayerEntity) entitylivingbaseIn, bufferIn);
		}
	}

	@Inject(at = @At("RETURN"),
			method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void postRender(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(entitylivingbaseIn instanceof AbstractClientPlayerEntity) {
			ClientProxy.INSTANCE.unbind(modelArmor);
			ClientProxy.INSTANCE.unbind(modelLeggings);
		}
	}

	@Inject(at = @At("HEAD"),
			method = "renderArmor(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
					+ "ZLnet/minecraft/client/renderer/entity/model/BipedModel;FFFLnet/minecraft/util/ResourceLocation;)V", remap = false)
	private void preRender(MatrixStack p_241738_1_, IRenderTypeBuffer p_241738_2_, int p_241738_3_, boolean p_241738_5_, BipedModel<LivingEntity> model, float p_241738_8_, float p_241738_9_, float p_241738_10_, ResourceLocation resLoc, CallbackInfo cbi) {
		ClientProxy.mc.getPlayerRenderManager().bindSkin(model, new CallbackInfoReturnable<>(null, true, resLoc), model == modelLeggings ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
	}

	@Inject(at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/layers/ArmorLayer;isLegSlot(Lnet/minecraft/inventory/EquipmentSlotType;)Z"),
			method = "renderArmorPart(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFFLnet/minecraft/inventory/EquipmentSlotType;I)V",
					locals = LocalCapture.CAPTURE_FAILHARD)
	private void setupModel(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, LivingEntity entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, EquipmentSlotType slotIn, int packedLightIn, CallbackInfo cbi, ItemStack itemstack, ArmorItem armoritem, BipedModel<LivingEntity> a) {
		BipedModel<LivingEntity> model = getEntityModel();
		a.bipedHead.copyModelAngles(model.bipedHead);
		a.bipedHeadwear.copyModelAngles(model.bipedHeadwear);
		a.bipedBody.copyModelAngles(model.bipedBody);
		a.bipedRightArm.copyModelAngles(model.bipedRightArm);
		a.bipedLeftArm.copyModelAngles(model.bipedLeftArm);
		a.bipedRightLeg.copyModelAngles(model.bipedRightLeg);
		a.bipedLeftLeg.copyModelAngles(model.bipedLeftLeg);
	}
}
