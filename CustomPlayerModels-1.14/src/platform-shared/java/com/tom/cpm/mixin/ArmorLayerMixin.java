package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArmorLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.IBipedModel;
import com.tom.cpm.client.PlayerRenderManager;

@Mixin(ArmorLayer.class)
public abstract class ArmorLayerMixin extends LayerRenderer<LivingEntity, BipedModel<LivingEntity>> {

	public ArmorLayerMixin(IEntityRenderer<LivingEntity, BipedModel<LivingEntity>> p_i50926_1_) {
		super(p_i50926_1_);
	}

	private @Final @Shadow BipedModel<LivingEntity> innerModel;
	private @Final @Shadow BipedModel<LivingEntity> outerModel;

	@Inject(at = @At("HEAD"),
			method = "render(Lnet/minecraft/entity/LivingEntity;FFFFFFF)V")
	public void preRender(LivingEntity entitylivingbaseIn, float f1, float f2, float f3, float f4, float f5, float f6, float f7, CallbackInfo cbi) {
		if(getParentModel() instanceof BipedModel) {
			CustomPlayerModelsClient.INSTANCE.renderArmor(outerModel, innerModel, getParentModel());
		}
	}

	@Inject(at = @At("RETURN"),
			method = "render(Lnet/minecraft/entity/LivingEntity;FFFFFFF)V")
	public void postRender(LivingEntity entitylivingbaseIn, float f1, float f2, float f3, float f4, float f5, float f6, float f7, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbind(outerModel);
		CustomPlayerModelsClient.INSTANCE.manager.unbind(innerModel);
	}

	@Inject(at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/layers/ArmorLayer;usesInnerModel(Lnet/minecraft/inventory/EquipmentSlotType;)Z"),
			method = "renderArmorPiece(Lnet/minecraft/entity/LivingEntity;FFFFFFFLnet/minecraft/inventory/EquipmentSlotType;)V",
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void setupModel(LivingEntity entityLivingBaseIn, float f1, float f2, float f3, float f4, float f5, float f6, float f7, EquipmentSlotType slotIn, CallbackInfo cbi, ItemStack itemstack, ArmorItem armoritem, BipedModel<LivingEntity> armor) {
		BipedModel<LivingEntity> model = getParentModel();
		((IBipedModel) armor).cpm$noSetup();
		PlayerRenderManager m = CustomPlayerModelsClient.mc.getPlayerRenderManager();
		m.copyModelForArmor(model.body, armor.body);
		m.copyModelForArmor(model.head, armor.head);
		m.copyModelForArmor(model.leftArm, armor.leftArm);
		m.copyModelForArmor(model.leftLeg, armor.leftLeg);
		m.copyModelForArmor(model.rightArm, armor.rightArm);
		m.copyModelForArmor(model.rightLeg, armor.rightLeg);
	}
}
