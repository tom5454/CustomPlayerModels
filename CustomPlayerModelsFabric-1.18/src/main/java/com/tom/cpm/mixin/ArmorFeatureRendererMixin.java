package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin {

	private @Final @Shadow BipedEntityModel<LivingEntity> leggingsModel;
	private @Final @Shadow BipedEntityModel<LivingEntity> bodyModel;

	@Shadow abstract Identifier getArmorTexture(ArmorItem armorItem, boolean bl, String string);

	@Inject(at = @At("HEAD"),
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void preRender(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(entitylivingbaseIn instanceof AbstractClientPlayerEntity) {
			CustomPlayerModelsClient.INSTANCE.renderArmor(bodyModel, leggingsModel, (PlayerEntity) entitylivingbaseIn, bufferIn);
		}
	}

	@Inject(at = @At("RETURN"),
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void postRender(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(entitylivingbaseIn instanceof AbstractClientPlayerEntity) {
			CustomPlayerModelsClient.INSTANCE.unbind(bodyModel);
			CustomPlayerModelsClient.INSTANCE.unbind(leggingsModel);
		}
	}

	@Inject(at = @At("HEAD"),
			method = "renderArmorParts(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
					+ "Lnet/minecraft/item/ArmorItem;ZLnet/minecraft/client/render/entity/model/BipedEntityModel;ZFFFLjava/lang/String;)V")
	private void preRender(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i,
			ArmorItem armorItem, boolean bl, BipedEntityModel<LivingEntity> model, boolean bl2, float f, float g, float h,
			String string, CallbackInfo cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, new ModelTexture(this.getArmorTexture(armorItem, bl2, string), PlayerRenderManager.armor), model == leggingsModel ? TextureSheetType.ARMOR2 : TextureSheetType.ARMOR1);
	}
}
