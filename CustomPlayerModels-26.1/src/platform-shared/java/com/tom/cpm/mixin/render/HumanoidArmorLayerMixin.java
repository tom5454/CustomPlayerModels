package com.tom.cpm.mixin.render;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin extends RenderLayer<HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

	public HumanoidArmorLayerMixin(
			RenderLayerParent<HumanoidRenderState, HumanoidModel<HumanoidRenderState>> renderLayerParent) {
		super(renderLayerParent);
	}

	private @Final @Shadow ArmorModelSet<HumanoidModel<HumanoidRenderState>> modelSet;

	@Inject(at = @At("HEAD"), method = "submit")
	public void preRender(PoseStack p_435921_, SubmitNodeCollector p_434130_, int p_434678_, HumanoidRenderState p_435902_, float p_435802_, float p_434554_, CallbackInfo cbi) {
		if(getParentModel() instanceof HumanoidModel) {
			CustomPlayerModelsClient.INSTANCE.renderArmor(modelSet, getParentModel());
		}
	}

	@Inject(at = @At("RETURN"), method = "submit")
	public void postRender(PoseStack p_435921_, SubmitNodeCollector p_434130_, int p_434678_, HumanoidRenderState p_435902_, float p_435802_, float p_434554_, CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.manager.unbind(modelSet.head());
		CustomPlayerModelsClient.INSTANCE.manager.unbind(modelSet.chest());
		CustomPlayerModelsClient.INSTANCE.manager.unbind(modelSet.legs());
		CustomPlayerModelsClient.INSTANCE.manager.unbind(modelSet.feet());
	}
}
