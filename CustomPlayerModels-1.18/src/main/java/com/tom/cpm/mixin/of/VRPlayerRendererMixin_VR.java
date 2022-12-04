package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.render.VRPlayerRenderer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.CustomPlayerModelsClient.PlayerNameTagRenderer;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(VRPlayerRenderer.class)
public abstract class VRPlayerRendererMixin_VR extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> implements PlayerNameTagRenderer<AbstractClientPlayer> {

	public VRPlayerRendererMixin_VR(Context pContext, PlayerModel<AbstractClientPlayer> pModel, float pShadowRadius) {
		super(pContext, pModel, pShadowRadius);
	}

	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FF"
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			remap = false)
	public void onRenderPre(AbstractClientPlayer entityIn, float entityYaw, float partialTicks, PoseStack PoseStackIn, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.bindPlayer(entityIn, bufferIn, getModel());
	}

	@Inject(at = @At("RETURN"), method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FF"
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			remap = false)
	public void onRenderPost(AbstractClientPlayer entityIn, float entityYaw, float partialTicks, PoseStack PoseStackIn, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(getModel());
	}

	@Inject(
			at = @At("RETURN"),
			method = {
					"getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;"
			},
			cancellable = true, remap = false)
	public void onGetEntityTexture(AbstractClientPlayer entity, CallbackInfoReturnable<ResourceLocation> cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(getModel(), new ModelTexture(cbi), TextureSheetType.SKIN);
	}

	@Redirect(at =
			@At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/AbstractClientPlayer;"
							+ "getSkinTextureLocation()Lnet/minecraft/resources/ResourceLocation;",
							remap = true
					),
			method = "renderHand("
					+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;"
					+ "ILnet/minecraft/client/player/AbstractClientPlayer;"
					+ "Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;)V",
					remap = false
			)
	public ResourceLocation getSkinTex(AbstractClientPlayer player) {
		return getTextureLocation(player);
	}

	@Inject(at = @At("HEAD"), method = {
			"renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V"
	}, remap = false)
	public void onRenderRightArmPre(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo cbi) {
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = true;
		CustomPlayerModelsClient.INSTANCE.manager.bindHand(player, vertexConsumers, getModel());
	}

	@Inject(at = @At("HEAD"), method = {
			"renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V"
	}, remap = false)
	public void onRenderLeftArmPre(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo cbi) {
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = true;
		CustomPlayerModelsClient.INSTANCE.manager.bindHand(player, vertexConsumers, getModel());
	}

	@Inject(at = @At("RETURN"), method = {
			"renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V"
	}, remap = false)
	public void onRenderRightArmPost(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(getModel());
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = false;
	}

	@Inject(at = @At("RETURN"), method = {
			"renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V"
	}, remap = false)
	public void onRenderLeftArmPost(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(getModel());
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = false;
	}

	@Inject(at = @At("HEAD"), method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", cancellable = true, remap = false)
	public void onRenderName1(AbstractClientPlayer entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo cbi) {
		if(!Player.isEnableNames())cbi.cancel();
	}

	@Inject(at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;renderNameTag(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			ordinal = 1, remap = true),
			method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", remap = false)
	public void onRenderName2(AbstractClientPlayer entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo cbi) {
		if(Player.isEnableLoadingInfo())
			CustomPlayerModelsClient.renderNameTag(this, entityIn, entityIn.getGameProfile(), ModelDefinitionLoader.PLAYER_UNIQUE, matrixStackIn, bufferIn, packedLightIn);
	}

	@Redirect(at =
			@At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/RenderType;entitySolid("
							+ "Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",
							remap = true
					),
			method = "renderHand("
					+ "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;"
					+ "ILnet/minecraft/client/player/AbstractClientPlayer;"
					+ "Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;)V",
					require = 0,
					remap = false
			)
	public RenderType getArmLayer(ResourceLocation loc, PoseStack PoseStackIn, MultiBufferSource bufferIn, int combinedLightIn, AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn) {
		return CustomPlayerModelsClient.mc.getPlayerRenderManager().isBound(getModel()) ? RenderType.entityTranslucent(getTextureLocation(playerIn)) : RenderType.entitySolid(getTextureLocation(playerIn));
	}

	@Override
	public void cpm$renderNameTag(AbstractClientPlayer entityIn, Component displayNameIn, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int packedLightIn) {
		super.renderNameTag(entityIn, displayNameIn, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public EntityRenderDispatcher cpm$entityRenderDispatcher() {
		return entityRenderDispatcher;
	}
}
