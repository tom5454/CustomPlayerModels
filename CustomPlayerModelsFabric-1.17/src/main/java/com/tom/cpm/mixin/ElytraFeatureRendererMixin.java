package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(value = ElytraFeatureRenderer.class, priority = 900)
public abstract class ElytraFeatureRendererMixin extends FeatureRenderer<LivingEntity, EntityModel<LivingEntity>> {
	public ElytraFeatureRendererMixin(FeatureRendererContext<LivingEntity, EntityModel<LivingEntity>> context) {
		super(context);
	}
	private @Shadow @Final ElytraEntityModel<LivingEntity> elytra;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"),
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void preRender(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		if(getContextModel() instanceof BipedEntityModel)CustomPlayerModelsClient.INSTANCE.renderElytra((BipedEntityModel<LivingEntity>) getContextModel(), elytra);
	}

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"),
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	public void postRender(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbind(elytra);
	}

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/RenderLayer;getArmorCutoutNoCull("
					+ "Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
					ordinal = 0
			),
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I"
					+ "Lnet/minecraft/entity/LivingEntity;FFFFFF)V")
	private RenderLayer onGetRenderTypeNoSkin(Identifier resLoc, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if(getContextModel() instanceof BipedEntityModel) {
			ModelTexture mt = new ModelTexture(resLoc, PlayerRenderManager.armor);
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(elytra, mt, TextureSheetType.ELYTRA);
			return mt.getRenderType();
		}
		return RenderLayer.getArmorCutoutNoCull(resLoc);
	}
}
