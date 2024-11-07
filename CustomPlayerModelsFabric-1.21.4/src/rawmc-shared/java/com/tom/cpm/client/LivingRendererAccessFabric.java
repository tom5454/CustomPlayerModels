package com.tom.cpm.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import com.mojang.blaze3d.vertex.PoseStack;

public interface LivingRendererAccessFabric {
	default void cpm$renderPre(LivingEntityRenderState state, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i) {}
	default void cpm$renderPost(LivingEntityRenderState state, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i) {}
}
