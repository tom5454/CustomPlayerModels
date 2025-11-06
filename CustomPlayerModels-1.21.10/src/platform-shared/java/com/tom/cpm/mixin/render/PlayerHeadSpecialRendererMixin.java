package com.tom.cpm.mixin.render;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;

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
					+ "Lnet/minecraft/core/Direction;FFLcom/mojang/blaze3d/vertex/PoseStack;"
					+ "Lnet/minecraft/client/renderer/SubmitNodeCollector;I"
					+ "Lnet/minecraft/client/model/SkullModelBase;Lnet/minecraft/client/renderer/RenderType;I"
					+ "Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), method = "submit")
	public void submitPre(
			@Nullable Direction p1,
			float p2,
			float p3,
			PoseStack p4,
			SubmitNodeCollector collector,
			int p6,
			SkullModelBase p7,
			RenderType renderType,
			int p9,
			@Nullable ModelFeatureRenderer.CrumblingOverlay p10,
			Operation<Void> op,
			@Local PlayerSkinRenderCache.RenderInfo info
			) {
		if (info != null) {
			collector = new CPMSubmitNodeCollector(collector);
			CustomPlayerModelsClient.INSTANCE.manager.bindSkull(info.gameProfile(), null, modelBase);
			ModelTexture mt = new ModelTexture(info.playerSkin().body().texturePath());
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(modelBase, mt, TextureSheetType.SKIN);
			renderType = mt.getRenderType();
		}
		op.call(p1, p2, p3, p4, collector, p6, p7, renderType, p9, p10);
	}

	@Inject(at = @At("RETURN"), method = "submit")
	public void submitPost(
			@Nullable PlayerSkinRenderCache.RenderInfo p_451691_,
			ItemDisplayContext p_439036_,
			PoseStack p_439012_,
			SubmitNodeCollector p_439704_,
			int p_439598_,
			int p_440685_,
			boolean p_439388_,
			int p_451687_,
			CallbackInfo cbi
			) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().unbindModel(modelBase);
	}
}
