package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer.PlayerHeadRenderInfo;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.PlayerHeadRenderInfoAccess;
import com.tom.cpm.client.RefHolder;

@Mixin(PlayerHeadSpecialRenderer.class)
public class PlayerHeadSpecialRendererMixin {
	private @Shadow @Final SkullModelBase modelBase;

	@Inject(at = @At("HEAD"), method = "render")
	public void onRender(
			PlayerHeadRenderInfo info,
			final ItemDisplayContext itemDisplayContext, final PoseStack poseStack,
			final MultiBufferSource multiBufferSource, final int i, final int j, final boolean bl,
			CallbackInfo cbi, @Local final LocalRef<PlayerHeadRenderInfo> playerHeadRenderInfo
			) {
		Object b = playerHeadRenderInfo.get();
		if (b instanceof PlayerHeadRenderInfoAccess a) {
			ResolvableProfile resolvableProfile = a.cpm$getProfile();
			GameProfile gameProfile = resolvableProfile != null ? resolvableProfile.gameProfile() : null;
			if (gameProfile != null) {
				RefHolder.CPM_MODELS = t -> t == SkullBlock.Types.PLAYER ? modelBase : null;
				CustomPlayerModelsClient.INSTANCE.renderSkull(modelBase, gameProfile, multiBufferSource);
				playerHeadRenderInfo.set(new PlayerHeadRenderInfo(SkullBlockRenderer.getRenderType(SkullBlock.Types.PLAYER, resolvableProfile)));
			}
		}
	}

	@Inject(at = @At("RETURN"), method = "createAndCacheIfTextureIsUnpacked(Lnet/minecraft/world/item/component/ResolvableProfile;)Lnet/minecraft/client/renderer/special/PlayerHeadSpecialRenderer$PlayerHeadRenderInfo;")
	private void onCreateAndCacheIfTextureIsUnpacked(ResolvableProfile resolvableProfile, CallbackInfoReturnable<PlayerHeadRenderInfo> cbi) {
		Object b = cbi.getReturnValue();
		if (b instanceof PlayerHeadRenderInfoAccess a) {
			a.cpm$setProfile(resolvableProfile);
		}
	}
}
