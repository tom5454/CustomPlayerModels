package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.LivingRendererAccessFabric;
import com.tom.cpm.client.PlayerRenderStateAccess;

@Mixin(value = PlayerRenderer.class, priority = 900)
public abstract class PlayerRendererMixinFabric extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> implements LivingRendererAccessFabric {

	public PlayerRendererMixinFabric(Context context, PlayerModel entityModel, float f) {
		super(context, entityModel, f);
	}

	@Override
	public void cpm$renderPre(LivingEntityRenderState state, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		PlayerRenderStateAccess sa = (PlayerRenderStateAccess) state;
		if (sa.cpm$getPlayer() != null) {
			CustomPlayerModelsClient.INSTANCE.manager.bindPlayerState(sa.cpm$getPlayer(), multiBufferSource, getModel(), null);
		}
	}

	@Override
	public void cpm$renderPost(LivingEntityRenderState state, PoseStack poseStack, MultiBufferSource multiBufferSource,
			int i) {
		CustomPlayerModelsClient.INSTANCE.playerRenderPost(multiBufferSource, getModel());
	}
}
