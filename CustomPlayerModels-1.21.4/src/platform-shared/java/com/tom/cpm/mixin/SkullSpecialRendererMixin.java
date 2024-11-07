package com.tom.cpm.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SkullSpecialRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.RefHolder;

@Mixin(SkullSpecialRenderer.class)
public class SkullSpecialRendererMixin {
	private @Shadow @Final SkullBlock.Type skullType;
	private @Shadow @Final SkullModelBase model;

	@Inject(at = @At("HEAD"), method = "render")
	public void onRender(
			@Nullable ResolvableProfile resolvableProfile,
			ItemDisplayContext itemDisplayContext,
			PoseStack poseStack,
			MultiBufferSource multiBufferSource,
			int i,
			int j,
			boolean bl,
			CallbackInfo cbi
			) {
		if (skullType == SkullBlock.Types.PLAYER) {
			RefHolder.CPM_MODELS = t -> t == skullType ? model : null;
			GameProfile gameProfile = resolvableProfile != null ? resolvableProfile.gameProfile() : null;
			if (gameProfile != null) {
				CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfile, multiBufferSource);
			}
		}
	}
}
