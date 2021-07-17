package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(value = PlayerEntityRenderer.class, priority = 900)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	public PlayerRendererMixin(Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
		super(ctx, model, shadowRadius);
	}

	@Inject(at = @At("RETURN"), method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", cancellable = true)
	public void onGetEntityTexture(AbstractClientPlayerEntity entity, CallbackInfoReturnable<Identifier> cbi) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(getModel(), cbi, TextureSheetType.SKIN);
	}

	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void onRenderPre(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.playerRenderPre(abstractClientPlayerEntity, vertexConsumerProvider);
	}

	@Inject(at = @At("RETURN"), method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void onRenderPost(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.playerRenderPost();
	}

	@Inject(at = @At("HEAD"), method = "renderRightArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V")
	public void onRenderRightArmPre(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHand(vertexConsumers);
	}

	@Inject(at = @At("HEAD"), method = "renderLeftArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V")
	public void onRenderLeftArmPre(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.renderHand(vertexConsumers);
	}

	@Inject(at = @At("RETURN"), method = "renderRightArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V")
	public void onRenderRightArmPost(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.unbind(getModel());
	}

	@Inject(at = @At("RETURN"), method = "renderLeftArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V")
	public void onRenderLeftArmPost(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.unbind(getModel());
	}

	@Redirect(at =
			@At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;"
							+ "getSkinTexture()Lnet/minecraft/util/Identifier;"
					),
			method = "renderArm("
					+ "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;"
					+ "ILnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/model/ModelPart;"
					+ "Lnet/minecraft/client/model/ModelPart;)V"
			)
	public Identifier getSkinTex(AbstractClientPlayerEntity player) {
		return getTexture(player);
	}

	@Redirect(at =
			@At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/RenderLayer;getEntitySolid("
							+ "Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
					),
			method = "renderArm("
					+ "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;"
					+ "ILnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/model/ModelPart;"
					+ "Lnet/minecraft/client/model/ModelPart;)V",
					require = 0
			)
	public RenderLayer getArmLayer(Identifier loc, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity playerIn, ModelPart arm, ModelPart sleeve) {
		return CustomPlayerModelsClient.mc.getPlayerRenderManager().isBound(getModel()) ? RenderLayer.getEntityTranslucent(getTexture(playerIn)) : RenderLayer.getEntitySolid(getTexture(playerIn));
	}

	@Inject(at = @At("HEAD"), method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", cancellable = true)
	public void onRenderName(AbstractClientPlayerEntity entityIn, Text displayNameIn, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, CallbackInfo cbi) {
		if(!Player.isEnableNames())cbi.cancel();
	}
}
