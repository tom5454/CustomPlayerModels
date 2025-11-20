package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Avatar;

import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.cpm.client.CPMOrderedSubmitNodeCollector.CPMSubmitNodeCollector;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.PlayerRenderStateAccess;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererFabric<AvatarlikeEntity extends Avatar & ClientAvatarEntity>
extends LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerModel>
implements com.tom.cpm.client.EntityRenderer<AvatarRenderState> {

	public AvatarRendererFabric(Context context, PlayerModel entityModel, float f) {
		super(context, entityModel, f);
	}

	@Override
	public void cpm$onSubmitPre(AvatarRenderState livingEntityRenderState, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState,
			LocalRef<SubmitNodeCollector> snc) {
		snc.set(new CPMSubmitNodeCollector(submitNodeCollector));
		CustomPlayerModelsClient.INSTANCE.playerRenderPre((PlayerRenderStateAccess) livingEntityRenderState, model, livingEntityRenderState);
	}

	@Override
	public void cpm$onSubmitPost(AvatarRenderState livingEntityRenderState, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
		CustomPlayerModelsClient.INSTANCE.playerRenderPost(model);
	}
}
