package com.tom.cpm.mixin;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import com.tom.cpm.client.GuiGraphicsEx;
import com.tom.cpm.client.GuiRenderStates.Colored4RectangleRenderState;
import com.tom.cpm.client.GuiRenderStates.ColoredRectangleFRenderState;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements GuiGraphicsEx {
	private @Shadow @Final GuiGraphics.ScissorStack scissorStack;
	private @Shadow @Final GuiRenderState guiRenderState;
	private @Shadow @Final Matrix3x2fStack pose;

	@Override
	public void cpm$fillGradient(RenderPipeline renderPipeline, TextureSetup textureSetup, int x0, int y0, int x1, int y1,
			int topLeft, int topRight, int bottomLeft, int bottomRight) {
		this.guiRenderState.submitGuiElement(
				new Colored4RectangleRenderState(
						renderPipeline, textureSetup, new Matrix3x2f(this.pose), x0, y0, x1, y1, topLeft, topRight, bottomLeft, bottomRight, this.scissorStack.peek()
						)
				);
	}

	@Override
	public void cpm$fill(RenderPipeline renderPipeline, TextureSetup textureSetup, float x0, float y0, float x1, float y1,
			int color) {
		this.guiRenderState.submitGuiElement(
				new ColoredRectangleFRenderState(
						renderPipeline, textureSetup, new Matrix3x2f(this.pose), x0, y0, x1, y1, color, this.scissorStack.peek()
						)
				);
	}

	@Override
	public <I, S extends PictureInPictureRenderState> void cpm$drawPip(I instance, int x0, int y0, int x1, int y1,
			StateFactory<I, S> sf) {
		this.guiRenderState.submitPicturesInPictureState(sf.create(instance, x0, y0, x1, y1, this.scissorStack.peek()));
	}
}
