package com.tom.cpm.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(SkullBlockEntityRenderer.class)
public abstract class SkullBlockEntityRendererMixin extends BlockEntityRenderer<BlockEntity> {

	public SkullBlockEntityRendererMixin(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Shadow private static @Final Map<SkullBlock.SkullType, SkullEntityModel> MODELS;

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/RenderLayer;getEntityTranslucent("
					+ "Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
			),
			method = "method_3578(Lnet/minecraft/block/SkullBlock$SkullType;Lcom/mojang/authlib/GameProfile;)"
					+ "Lnet/minecraft/client/render/RenderLayer;")
	private static RenderLayer onGetRenderType(Identifier resLoc, SkullBlock.SkullType skullType, GameProfile gameProfileIn) {
		SkullEntityModel model = MODELS.get(skullType);
		CallbackInfoReturnable<Identifier> cbi = new CallbackInfoReturnable<>(null, true, resLoc);
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, cbi);
		return RenderLayer.getEntityTranslucent(cbi.getReturnValue());
	}

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/RenderLayer;getEntityCutoutNoCull("
					+ "Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
					ordinal = 0
			),
			method = "method_3578(Lnet/minecraft/block/SkullBlock$SkullType;Lcom/mojang/authlib/GameProfile;)"
					+ "Lnet/minecraft/client/render/RenderLayer;")
	private static RenderLayer onGetRenderTypeNoSkin(Identifier resLoc, SkullBlock.SkullType skullType, GameProfile gameProfileIn) {
		SkullEntityModel model = MODELS.get(skullType);
		CallbackInfoReturnable<Identifier> cbi = new CallbackInfoReturnable<>(null, true, resLoc);
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, cbi);
		return RenderLayer.getEntityTranslucent(cbi.getReturnValue());
	}

	@Inject(at = @At("HEAD"),
			method = "render(Lnet/minecraft/util/math/Direction;FLnet/minecraft/block/SkullBlock$SkullType;"
					+ "Lcom/mojang/authlib/GameProfile;FLnet/minecraft/client/util/math/MatrixStack;"
					+ "Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	private static void onRenderPre(Direction directionIn, float p_228879_1_, SkullBlock.SkullType skullType, GameProfile gameProfileIn, float animationProgress, MatrixStack matrixStackIn, VertexConsumerProvider buffer, int combinedLight, CallbackInfo cbi) {
		if (skullType == SkullBlock.Type.PLAYER && gameProfileIn != null) {
			SkullEntityModel model = MODELS.get(skullType);
			CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfileIn, buffer);
		}
	}

	@Inject(at = @At("RETURN"),
			method = "render(Lnet/minecraft/util/math/Direction;FLnet/minecraft/block/SkullBlock$SkullType;"
					+ "Lcom/mojang/authlib/GameProfile;FLnet/minecraft/client/util/math/MatrixStack;"
					+ "Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	private static void onRenderPost(Direction directionIn, float p_228879_1_, SkullBlock.SkullType skullType, GameProfile gameProfileIn, float animationProgress, MatrixStack matrixStackIn, VertexConsumerProvider buffer, int combinedLight, CallbackInfo cbi) {
		if (skullType == SkullBlock.Type.PLAYER && gameProfileIn != null) {
			SkullEntityModel model = MODELS.get(skullType);
			CustomPlayerModelsClient.INSTANCE.unbind(model);
		}
	}
}
