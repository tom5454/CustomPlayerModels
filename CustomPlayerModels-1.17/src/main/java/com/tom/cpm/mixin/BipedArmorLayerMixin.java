package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(HumanoidArmorLayer.class)
public abstract class BipedArmorLayerMixin extends RenderLayer<LivingEntity, HumanoidModel<LivingEntity>> {

	public BipedArmorLayerMixin(RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> p_117346_) {
		super(p_117346_);
	}

	private @Final @Shadow HumanoidModel<LivingEntity> innerModel;
	private @Final @Shadow HumanoidModel<LivingEntity> outerModel;

	@Inject(at = @At("HEAD"),
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
					+ "Lnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
	public void preRender(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(getParentModel() instanceof HumanoidModel) {
			ClientProxy.INSTANCE.renderArmor(outerModel, innerModel, getParentModel());
		}
	}

	@Inject(at = @At("RETURN"),
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
					+ "Lnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
	public void postRender(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		ClientProxy.INSTANCE.manager.unbind(outerModel);
		ClientProxy.INSTANCE.manager.unbind(innerModel);
	}

	@Inject(at = @At("HEAD"),
			method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
					+ "ZLnet/minecraft/client/model/HumanoidModel;FFFLnet/minecraft/resources/ResourceLocation;)V", remap = false)
	private void preRender(PoseStack p_241738_1_, MultiBufferSource p_241738_2_, int p_241738_3_, boolean p_241738_5_, HumanoidModel<LivingEntity> model, float p_241738_8_, float p_241738_9_, float p_241738_10_, ResourceLocation resLoc, CallbackInfo cbi) {
		ClientProxy.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(resLoc, PlayerRenderManager.armor), model == innerModel ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
	}
}
