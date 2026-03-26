package com.tom.cpm.mixin.render;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CPMOrderedSubmitNodeCollector.CPMSubmitNodeCollector;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(PlayerHeadSpecialRenderer.class)
public class PlayerHeadSpecialRendererMixin {
	private @Shadow @Final SkullModelBase modelBase;

	@WrapOperation(at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;submitSkull("
					+ "FLcom/mojang/blaze3d/vertex/PoseStack;"
					+ "Lnet/minecraft/client/renderer/SubmitNodeCollector;I"
					+ "Lnet/minecraft/client/model/object/skull/SkullModelBase;"
					+ "Lnet/minecraft/client/renderer/rendertype/RenderType;I"
					+ "Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), method = "submit")
	public void submitPre(
			float animationValue,
			PoseStack poseStack,
			SubmitNodeCollector collector,
			int lightCoords,
			SkullModelBase model,
			RenderType renderType,
			int outlineColor,
			ModelFeatureRenderer.CrumblingOverlay breakProgress,
			Operation<Void> op,
			@Local PlayerSkinRenderCache.RenderInfo info
			) {
		if (info != null) {
			if (!(collector instanceof CPMSubmitNodeCollector))
				collector = new CPMSubmitNodeCollector(collector);
			CustomPlayerModelsClient.INSTANCE.manager.bindSkull(info.gameProfile(), null, modelBase);
			ModelTexture mt = new ModelTexture(info.playerSkin().body().texturePath());
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(modelBase, mt, TextureSheetType.SKIN);
			renderType = mt.getRenderType();
		}
		op.call(animationValue, poseStack, collector, lightCoords, model, renderType, outlineColor, breakProgress);
	}

	@Inject(at = @At("RETURN"), method = "submit")
	public void submitPost(
			final PlayerSkinRenderCache.RenderInfo argument,
			final PoseStack poseStack,
			final SubmitNodeCollector submitNodeCollector,
			final int lightCoords,
			final int overlayCoords,
			final boolean hasFoil,
			final int outlineColor,
			CallbackInfo cbi
			) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().unbindModel(modelBase);
	}
}
