package com.tom.cpm.client;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;

import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

public interface EntityRenderer<S extends LivingEntityRenderState> {
	default void cpm$onSubmitPre(S livingEntityRenderState, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState,
			LocalRef<SubmitNodeCollector> snc) {}
	default void cpm$onSubmitPost(S livingEntityRenderState, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {}
}
