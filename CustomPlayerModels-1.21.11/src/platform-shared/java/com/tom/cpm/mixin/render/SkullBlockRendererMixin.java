package com.tom.cpm.mixin.render;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CPMOrderedSubmitNodeCollector.CPMSubmitNodeCollector;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.client.SkullBlockRenderStateAccess;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin implements BlockEntityRenderer<SkullBlockEntity, SkullBlockRenderState> {
	private @Shadow @Final PlayerSkinRenderCache playerSkinRenderCache;

	@Inject(at = @At("RETURN"), method = "extractRenderState")
	public void onExtractRenderState(
			SkullBlockEntity be, SkullBlockRenderState stateIn, float p_447371_, Vec3 p_445526_,
			@Nullable ModelFeatureRenderer.CrumblingOverlay p_446270_, CallbackInfo cbi
			) {
		ResolvableProfile resolvableprofile = be.getOwnerProfile();
		if (resolvableprofile != null) {
			var info = this.playerSkinRenderCache.getOrDefault(resolvableprofile);
			var pl = CustomPlayerModelsClient.INSTANCE.manager.loadSkull(info.gameProfile());
			((SkullBlockRenderStateAccess) stateIn).cpm$setPlayer(pl);
			((SkullBlockRenderStateAccess) stateIn).cpm$setSkin(info.playerSkin());
		}
	}

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;", shift = Shift.BY, by = 2), method = "submit(Lnet/minecraft/client/renderer/blockentity/state/SkullBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
	public void onSubmitPre(SkullBlockRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState cam, CallbackInfo cbi, @Local LocalRef<SubmitNodeCollector> snc, @Local SkullModelBase model) {
		SkullBlockRenderStateAccess sa = (SkullBlockRenderStateAccess) state;
		if (sa.cpm$getPlayer() != null) {
			CPMSubmitNodeCollector.injectSNC(snc);

			ModelTexture mt = new ModelTexture(sa.cpm$getSkin().body().texturePath());
			CustomPlayerModelsClient.INSTANCE.manager.bindPlayerState(sa.cpm$getPlayer(), null, model, null);
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, mt, TextureSheetType.SKIN);
			state.renderType = mt.getRenderType();
		}
	}

	@Inject(at = @At(value = "RETURN"), method = "submit(Lnet/minecraft/client/renderer/blockentity/state/SkullBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
	public void onSubmitPost(SkullBlockRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState cam, CallbackInfo cbi, @Local SkullModelBase model) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().unbindModel(model);
	}
}
