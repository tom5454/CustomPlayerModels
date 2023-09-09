package com.tom.cpm.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.coderbot.batchedentityrendering.impl.Groupable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin_Iris {
	private MultiBufferSource cpm$skullBufferCache;
	private boolean cpm$startedSkullGroup = false;

	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
	public <E extends BlockEntity> void renderPre(E pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, CallbackInfo cbi) {
		if (pBlockEntity instanceof SkullBlockEntity be && be.getOwnerProfile() != null && pBufferSource instanceof Groupable gr) {
			cpm$skullBufferCache = pBufferSource;
			cpm$startedSkullGroup = gr.maybeStartGroup();
			if (!cpm$startedSkullGroup) {
				gr.endGroup();
				gr.startGroup();
			}
		}
	}

	@Inject(at = @At("RETURN"), method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
	public <E extends BlockEntity> void renderPost(E pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource __, CallbackInfo cbi) {
		if (pBlockEntity instanceof SkullBlockEntity be && cpm$skullBufferCache != null) {
			Groupable gr = (Groupable) cpm$skullBufferCache;
			gr.endGroup();
			((MultiBufferSource.BufferSource) cpm$skullBufferCache).endBatch();
			if(!cpm$startedSkullGroup)gr.startGroup();
			cpm$skullBufferCache = null;
		}
	}
}
