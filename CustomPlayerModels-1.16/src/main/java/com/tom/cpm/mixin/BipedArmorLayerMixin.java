package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(BipedArmorLayer.class)
public abstract class BipedArmorLayerMixin extends LayerRenderer<LivingEntity, BipedModel<LivingEntity>> {

	public BipedArmorLayerMixin(IEntityRenderer<LivingEntity, BipedModel<LivingEntity>> p_i50926_1_) {
		super(p_i50926_1_);
	}

	private @Final @Shadow BipedModel<LivingEntity> innerModel;
	private @Final @Shadow BipedModel<LivingEntity> outerModel;

	@Inject(at = @At("HEAD"),
			method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void preRender(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		ClientProxy.INSTANCE.renderArmor(outerModel, innerModel, getParentModel());
	}

	@Inject(at = @At("RETURN"),
			method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void postRender(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		ClientProxy.INSTANCE.manager.unbind(outerModel);
		ClientProxy.INSTANCE.manager.unbind(innerModel);
	}

	@Inject(at = @At("HEAD"),
			method = {"renderModel(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
					+ "ZLnet/minecraft/client/renderer/entity/model/BipedModel;FFFLnet/minecraft/util/ResourceLocation;)V",
					"func_241738_a_(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I"
							+ "ZLnet/minecraft/client/renderer/entity/model/BipedModel;FFFLnet/minecraft/util/ResourceLocation;)V"}, remap = false)
	private void preRender(MatrixStack p_241738_1_, IRenderTypeBuffer p_241738_2_, int p_241738_3_, boolean p_241738_5_, BipedModel<LivingEntity> model, float p_241738_8_, float p_241738_9_, float p_241738_10_, ResourceLocation resLoc, CallbackInfo cbi) {
		ClientProxy.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(resLoc, PlayerRenderManager.armor), model == innerModel ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
	}
}
