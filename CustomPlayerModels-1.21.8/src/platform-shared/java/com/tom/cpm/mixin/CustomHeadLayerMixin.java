package com.tom.cpm.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.RefHolder;

@Mixin(CustomHeadLayer.class)
public class CustomHeadLayerMixin {
	private @Shadow @Final Function<SkullBlock.Type, SkullModelBase> skullModels;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;"
					+ "getRenderType(Lnet/minecraft/world/level/block/SkullBlock$Type;Lnet/minecraft/world/item/component/ResolvableProfile;)"
					+ "Lnet/minecraft/client/renderer/RenderType;"
			),
			method = "render")
	public void onRender(final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i,
			final LivingEntityRenderState livingEntityRenderState, final float f, final float g, CallbackInfo ci) {
		if(livingEntityRenderState.wornHeadType == SkullBlock.Types.PLAYER) {
			RefHolder.CPM_MODELS = skullModels;
			ResolvableProfile resolvableProfile = livingEntityRenderState.wornHeadProfile;
			GameProfile gameProfile = resolvableProfile != null ? resolvableProfile.gameProfile() : null;
			if(gameProfile != null) {
				SkullModelBase model = this.skullModels.apply(SkullBlock.Types.PLAYER);
				CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfile, multiBufferSource);
			}
		}
	}
}
