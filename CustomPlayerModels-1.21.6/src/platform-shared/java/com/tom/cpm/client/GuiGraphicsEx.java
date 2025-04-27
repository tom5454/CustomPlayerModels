package com.tom.cpm.client;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderPipelines;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import com.google.common.collect.ImmutableMap.Builder;

public interface GuiGraphicsEx {
	void cpm$fillGradient(RenderPipeline renderPipeline, TextureSetup textureSetup, int x0, int y0, int x1, int y1,
			int topLeft, int topRight, int bottomLeft, int bottomRight);

	default void cpm$fillGradient(int x0, int y0, int x1, int y1,
			int topLeft, int topRight, int bottomLeft, int bottomRight) {
		cpm$fillGradient(RenderPipelines.GUI, TextureSetup.noTexture(), x0, y0, x1, y1, topLeft, topRight, bottomLeft, bottomRight);
	}

	void cpm$fill(RenderPipeline renderPipeline, TextureSetup textureSetup, float x0, float y0, float x1, float y1, int color);

	default void cpm$fill(float x0, float y0, float x1, float y1, int color) {
		cpm$fill(RenderPipelines.GUI, TextureSetup.noTexture(), x0, y0, x1, y1, color);
	}

	<I, S extends PictureInPictureRenderState> void cpm$drawPip(I instance, int x0, int y0, int x1, int y1, StateFactory<I, S> sf);

	static void registerPip(BufferSource bufferSource, Builder<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> builder) {
		builder.put(Panel3dImpl.State.class, new Panel3dImpl.Renderer(bufferSource));
	}

	@FunctionalInterface
	public static interface StateFactory<I, S extends PictureInPictureRenderState> {
		S create(I instance, int x0, int y0, int x1, int y1, ScreenRectangle scissorArea);
	}
}