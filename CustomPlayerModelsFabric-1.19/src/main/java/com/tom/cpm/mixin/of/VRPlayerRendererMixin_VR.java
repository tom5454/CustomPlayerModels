package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.render.VRPlayerRenderer;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.CustomPlayerModelsClient.PlayerNameTagRenderer;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(VRPlayerRenderer.class)
public abstract class VRPlayerRendererMixin_VR extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> implements PlayerNameTagRenderer<AbstractClientPlayerEntity> {

	public VRPlayerRendererMixin_VR(Context pContext, PlayerEntityModel<AbstractClientPlayerEntity> pModel, float pShadowRadius) {
		super(pContext, pModel, pShadowRadius);
	}

	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void onRenderPre(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.bindPlayer(abstractClientPlayerEntity, vertexConsumerProvider, getModel());
	}

	@Inject(at = @At("RETURN"), method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void onRenderPost(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(getModel());
	}

	@Inject(
			at = @At("RETURN"),
			method = "getTextureLocation(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;",
			cancellable = true)
	public void onGetEntityTexture(AbstractClientPlayerEntity entity, CallbackInfoReturnable<Identifier> cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(getModel(), new ModelTexture(cbi), TextureSheetType.SKIN);
	}

	@Redirect(at =
			@At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;"
							+ "getSkinTexture()Lnet/minecraft/util/Identifier;"
					),
			method = "renderHand("
					+ "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;"
					+ "ILnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/model/ModelPart;"
					+ "Lnet/minecraft/client/model/ModelPart;)V"
			)
	public Identifier getSkinTex(AbstractClientPlayerEntity player) {
		return getTexture(player);
	}

	@Inject(at = @At("HEAD"), method = "renderRightHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V")
	public void onRenderRightArmPre(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = true;
		CustomPlayerModelsClient.INSTANCE.manager.bindHand(player, vertexConsumers, getModel());
	}

	@Inject(at = @At("HEAD"), method = "renderLeftHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V")
	public void onRenderLeftArmPre(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = true;
		CustomPlayerModelsClient.INSTANCE.manager.bindHand(player, vertexConsumers, getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderRightHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V")
	public void onRenderRightArmPost(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(getModel());
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = false;
	}

	@Inject(at = @At("RETURN"), method = "renderLeftHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V")
	public void onRenderLeftArmPost(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(getModel());
		com.tom.cpm.client.vr.VRPlayerRenderer.isFPHand = false;
	}

	@Inject(at = @At("HEAD"), method = "renderNameTag(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", cancellable = true)
	public void onRenderName1(AbstractClientPlayerEntity entityIn, Text displayNameIn, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, CallbackInfo cbi) {
		if(!Player.isEnableNames())cbi.cancel();
	}

	@Inject(at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
			ordinal = 1),
			method = "renderNameTag(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void onRenderName2(AbstractClientPlayerEntity entityIn, Text displayNameIn, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, CallbackInfo cbi) {
		if(Player.isEnableLoadingInfo())
			CustomPlayerModelsClient.renderNameTag(this, entityIn, entityIn.getGameProfile(), ModelDefinitionLoader.PLAYER_UNIQUE, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public void cpm$renderNameTag(AbstractClientPlayerEntity entityIn, Text displayNameIn, MatrixStack matrixStackIn,
			VertexConsumerProvider bufferIn, int packedLightIn) {
		super.renderLabelIfPresent(entityIn, displayNameIn, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public EntityRenderDispatcher cpm$entityRenderDispatcher() {
		return dispatcher;
	}

	@Redirect(at =
			@At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/RenderLayer;getEntitySolid("
							+ "Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
					),
			method = "renderHand("
					+ "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;"
					+ "ILnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/model/ModelPart;"
					+ "Lnet/minecraft/client/model/ModelPart;)V",
					require = 0
			)
	public RenderLayer getArmLayer(Identifier loc, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity playerIn, ModelPart arm, ModelPart sleeve) {
		return CustomPlayerModelsClient.mc.getPlayerRenderManager().isBound(getModel()) ? RenderLayer.getEntityTranslucent(getTexture(playerIn)) : RenderLayer.getEntitySolid(getTexture(playerIn));
	}
}
