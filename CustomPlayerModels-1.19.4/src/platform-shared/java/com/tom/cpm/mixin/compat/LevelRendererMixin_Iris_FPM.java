package com.tom.cpm.mixin.compat;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.coderbot.batchedentityrendering.impl.Groupable;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderBuffers;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.firstperson.FirstPersonModelCore;

@Mixin(value = LevelRenderer.class, priority = 500)
public class LevelRendererMixin_Iris_FPM {
	private @Shadow RenderBuffers renderBuffers;
	private @Unique boolean cpm$startedFPGroup = false;
	private @Unique boolean cpm$renderedFP = false;

	@Inject(method = "renderLevel",
			at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
			ordinal = 0)
			)
	public void renderPre(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera,
			GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo info) {
		if (!camera.isDetached() && FirstPersonModelCore.getWrapper().applyThirdPerson(false)) {
			BufferSource pBufferSource = this.renderBuffers.bufferSource();
			if (pBufferSource instanceof Groupable gr) {
				cpm$startedFPGroup = gr.maybeStartGroup();
				if (!cpm$startedFPGroup) {
					gr.endGroup();
					gr.startGroup();
				}
				cpm$renderedFP = true;
			}
		}
	}

	@Inject(method = "renderLevel",
			at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
			ordinal = 0, shift = Shift.AFTER)
			)
	public void renderPost(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera,
			GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo info) {
		if (cpm$renderedFP) {
			BufferSource pBufferSource = this.renderBuffers.bufferSource();
			if (pBufferSource instanceof Groupable gr) {
				gr.endGroup();
				pBufferSource.endBatch();
				if(!cpm$startedFPGroup)gr.startGroup();
			}
			cpm$startedFPGroup = false;
			cpm$renderedFP = false;
		}
	}
}
