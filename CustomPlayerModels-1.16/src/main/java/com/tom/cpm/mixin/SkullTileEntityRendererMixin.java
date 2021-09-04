package com.tom.cpm.mixin;

import java.util.Map;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.SkullBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.GenericHeadModel;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(SkullTileEntityRenderer.class)
public abstract class SkullTileEntityRendererMixin extends TileEntityRenderer<SkullTileEntity> {
	public SkullTileEntityRendererMixin(TileEntityRendererDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Shadow private static @Final Map<SkullBlock.ISkullType, GenericHeadModel> MODEL_BY_TYPE;

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent("
					+ "Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
			),
			method = "getRenderType(Lnet/minecraft/block/SkullBlock$ISkullType;Lcom/mojang/authlib/GameProfile;)"
					+ "Lnet/minecraft/client/renderer/RenderType;")
	private static RenderType onGetRenderType(ResourceLocation resLoc, SkullBlock.ISkullType skullType, @Nullable GameProfile gameProfileIn) {
		GenericHeadModel model = MODEL_BY_TYPE.get(skullType);
		ModelTexture mt = new ModelTexture(resLoc);
		ClientProxy.mc.getPlayerRenderManager().bindSkin(model, mt, TextureSheetType.SKIN);
		return mt.getRenderType();
	}

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull("
					+ "Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",
					ordinal = 0
			),
			method = "getRenderType(Lnet/minecraft/block/SkullBlock$ISkullType;Lcom/mojang/authlib/GameProfile;)"
					+ "Lnet/minecraft/client/renderer/RenderType;")
	private static RenderType onGetRenderTypeNoSkin(ResourceLocation resLoc, SkullBlock.ISkullType skullType, @Nullable GameProfile gameProfileIn) {
		GenericHeadModel model = MODEL_BY_TYPE.get(skullType);
		ModelTexture mt = new ModelTexture(resLoc);
		ClientProxy.mc.getPlayerRenderManager().bindSkin(model, mt, TextureSheetType.SKIN);
		return mt.getRenderType();
	}

	@Inject(at = @At("HEAD"),
			method = "renderSkull(Lnet/minecraft/util/Direction;FLnet/minecraft/block/SkullBlock$ISkullType;"
					+ "Lcom/mojang/authlib/GameProfile;FLcom/mojang/blaze3d/matrix/MatrixStack;"
					+ "Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V")
	private static void onRenderPre(@Nullable Direction directionIn, float p_228879_1_, SkullBlock.ISkullType skullType, @Nullable GameProfile gameProfileIn, float animationProgress, MatrixStack matrixStackIn, IRenderTypeBuffer buffer, int combinedLight, CallbackInfo cbi) {
		if (skullType == SkullBlock.Types.PLAYER && gameProfileIn != null) {
			GenericHeadModel model = MODEL_BY_TYPE.get(skullType);
			ClientProxy.INSTANCE.renderSkull(model, gameProfileIn, buffer);
		}
	}

	@Inject(at = @At("RETURN"),
			method = "renderSkull(Lnet/minecraft/util/Direction;FLnet/minecraft/block/SkullBlock$ISkullType;"
					+ "Lcom/mojang/authlib/GameProfile;FLcom/mojang/blaze3d/matrix/MatrixStack;"
					+ "Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V")
	private static void onRenderPost(@Nullable Direction directionIn, float p_228879_1_, SkullBlock.ISkullType skullType, @Nullable GameProfile gameProfileIn, float animationProgress, MatrixStack matrixStackIn, IRenderTypeBuffer buffer, int combinedLight, CallbackInfo cbi) {
		if (skullType == SkullBlock.Types.PLAYER && gameProfileIn != null) {
			GenericHeadModel model = MODEL_BY_TYPE.get(skullType);
			ClientProxy.INSTANCE.unbind(model);
		}
	}
}
