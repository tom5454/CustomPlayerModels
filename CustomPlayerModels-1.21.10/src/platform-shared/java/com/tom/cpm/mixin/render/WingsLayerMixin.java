package com.tom.cpm.mixin.render;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(WingsLayer.class)
public abstract class WingsLayerMixin<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
	private @Shadow @Final ElytraModel elytraModel;

	public WingsLayerMixin(RenderLayerParent<S, M> p_117346_) {
		super(p_117346_);
	}

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"),
			method = "submit")
	public void preRender(PoseStack p_435137_, SubmitNodeCollector p_434138_, int p_434689_, S p_434317_, float p_433309_, float p_432928_, CallbackInfo cbi) {
		if(getParentModel() instanceof HumanoidModel) {
			CustomPlayerModelsClient.INSTANCE.renderElytra((HumanoidModel<HumanoidRenderState>) getParentModel(), elytraModel);
		}
	}

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"),
			method = "submit")
	public void postRender(PoseStack p_435137_, SubmitNodeCollector p_434138_, int p_434689_, S p_434317_, float p_433309_, float p_432928_, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbind(elytraModel);
	}
}
