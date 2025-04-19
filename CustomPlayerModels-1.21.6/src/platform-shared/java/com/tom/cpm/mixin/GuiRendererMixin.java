package com.tom.cpm.mixin;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import com.tom.cpm.client.GuiGraphicsEx;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
	private @Shadow @Final @Mutable Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> pictureInPictureRenderers;

	@Inject(at = @At("RETURN"), method = "<init>")
	private void onInit(GuiRenderState guiRenderState, BufferSource bufferSource, List<PictureInPictureRenderer<?>> list, CallbackInfo cbi) {
		Builder<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> builder = ImmutableMap.builder();
		builder.putAll(pictureInPictureRenderers);
		GuiGraphicsEx.registerPip(bufferSource, builder);
		pictureInPictureRenderers = builder.buildOrThrow();
	}
}
