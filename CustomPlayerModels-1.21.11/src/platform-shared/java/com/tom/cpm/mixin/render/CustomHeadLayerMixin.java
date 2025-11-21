package com.tom.cpm.mixin.render;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CPMOrderedSubmitNodeCollector.CPMSubmitNodeCollector;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.model.TextureSheetType;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin<S extends LivingEntityRenderState, M extends EntityModel<S> & HeadedModel> extends RenderLayer<S, M> {
	private @Shadow @Final PlayerSkinRenderCache playerSkinRenderCache;

	public CustomHeadLayerMixin(RenderLayerParent<S, M> p_117346_) {
		super(p_117346_);
	}

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/entity/layers/CustomHeadLayer;resolveSkullRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lnet/minecraft/world/level/block/SkullBlock$Type;)Lnet/minecraft/client/renderer/rendertype/RenderType;"), method = "submit")
	public void onSubmitPre(PoseStack p_433907_, SubmitNodeCollector p_433246_, int p_435428_, S state,
			float p_434590_, float p_433023_, CallbackInfo cbi, @Local SkullModelBase model,
			@Local LocalRef<SubmitNodeCollector> snc, @Local LocalRef<RenderType> rt) {
		if (state.wornHeadType != SkullBlock.Types.PLAYER)return;
		ResolvableProfile resolvableprofile = state.wornHeadProfile;
		if (resolvableprofile != null) {
			CPMSubmitNodeCollector.injectSNC(snc);
			var info = this.playerSkinRenderCache.getOrDefault(resolvableprofile);
			CustomPlayerModelsClient.INSTANCE.manager.bindSkull(info.gameProfile(), null, model);
			ModelTexture mt = new ModelTexture(info.playerSkin().body().texturePath());
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, mt, TextureSheetType.SKIN);
			rt.set(mt.getRenderType());
		}
	}

	@Inject(at =
			@At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;submitSkull("
					+ "Lnet/minecraft/core/Direction;FFLcom/mojang/blaze3d/vertex/PoseStack;"
					+ "Lnet/minecraft/client/renderer/SubmitNodeCollector;I"
					+ "Lnet/minecraft/client/model/object/skull/SkullModelBase;"
					+ "Lnet/minecraft/client/renderer/rendertype/RenderType;I"
					+ "Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
					shift = Shift.AFTER),
			method = "submit")
	public void onSubmitPost(PoseStack p_433907_, SubmitNodeCollector p_433246_, int p_435428_, S state,
			float p_434590_, float p_433023_, CallbackInfo cbi, @Local SkullModelBase model) {
		CustomPlayerModelsClient.mc.getPlayerRenderManager().unbindModel(model);
	}
}
