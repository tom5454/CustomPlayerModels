package com.tom.cpm.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.SkullBlock;
import net.minecraft.client.renderer.entity.model.GenericHeadModel;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Direction;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(SkullTileEntityRenderer.class)
public abstract class SkullTileEntityRendererMixin extends TileEntityRenderer<SkullTileEntity> {

	@Shadow private static @Final Map<SkullBlock.ISkullType, GenericHeadModel> MODEL_BY_TYPE;

	@Inject(at = @At("HEAD"),
			method = "renderSkull(FFFLnet/minecraft/util/Direction;FLnet/minecraft/block/SkullBlock$ISkullType;"
					+ "Lcom/mojang/authlib/GameProfile;IF)V")
	private void onRenderPre(float f1, float f2, float f3, Direction directionIn, float p_228879_1_, SkullBlock.ISkullType skullType, GameProfile gameProfileIn, int i, float animationProgress, CallbackInfo cbi) {
		if (skullType == SkullBlock.Types.PLAYER && gameProfileIn != null) {
			GenericHeadModel model = MODEL_BY_TYPE.get(skullType);
			CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfileIn);
		}
	}

	@Inject(at = @At("RETURN"),
			method = "renderSkull(FFFLnet/minecraft/util/Direction;FLnet/minecraft/block/SkullBlock$ISkullType;"
					+ "Lcom/mojang/authlib/GameProfile;IF)V")
	private void onRenderPost(float f1, float f2, float f3, Direction directionIn, float p_228879_1_, SkullBlock.ISkullType skullType, GameProfile gameProfileIn, int i, float animationProgress, CallbackInfo cbi) {
		if (skullType == SkullBlock.Types.PLAYER && gameProfileIn != null) {
			GenericHeadModel model = MODEL_BY_TYPE.get(skullType);
			CustomPlayerModelsClient.INSTANCE.renderSkullPost(model);
		}
	}
}
