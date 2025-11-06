package com.tom.cpm.client;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class GuiRenderStates {
	public record Colored4RectangleRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose,
			int x0, int y0, int x1, int y1,
			ScreenRectangle bounds,
			int topLeft, int topRight, int bottomLeft, int bottomRight, @Nullable ScreenRectangle scissorArea
			) implements GuiElementRenderState {

		public Colored4RectangleRenderState(
				RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose,
				int x0, int y0,int x1, int y1,
				int topLeft, int topRight, int bottomLeft, int bottomRight, @Nullable ScreenRectangle scissorArea
				) {
			this(pipeline, textureSetup, pose, x0, y0, x1, y1,
					getBounds(x0, y0, x1, y1, pose, scissorArea),
					topLeft, topRight, bottomLeft, bottomRight, scissorArea);
		}

		@Override
		public void buildVertices(VertexConsumer vertexConsumer, float f) {
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0(), f).setColor(this.topLeft());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1(), f).setColor(this.bottomLeft());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1(), f).setColor(this.bottomRight());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0(), f).setColor(this.topRight());
		}
	}

	public record ColoredRectangleFRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, float x0, float y0, float x1, float y1, ScreenRectangle bounds,
			int color, @Nullable ScreenRectangle scissorArea
			) implements GuiElementRenderState {

		public ColoredRectangleFRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose,
				float x0, float y0, float x1, float y1,
				int color, @Nullable ScreenRectangle scissorArea) {
			this(pipeline, textureSetup, pose, x0, y0, x1, y1,
					getBounds(Mth.floor(x0), Mth.floor(y0), Mth.ceil(x1), Mth.ceil(y1), pose, scissorArea),
					color, scissorArea);
		}

		@Override
		public void buildVertices(VertexConsumer vertexConsumer, float f) {
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0(), f).setColor(this.color());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1(), f).setColor(this.color());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1(), f).setColor(this.color());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0(), f).setColor(this.color());
		}
	}

	public static ScreenRectangle getBounds(int x0, int y0, int x1, int y1, Matrix3x2f matrix3x2f, @Nullable ScreenRectangle scissor) {
		ScreenRectangle screenRectangle2 = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(matrix3x2f);
		return scissor != null ? scissor.intersection(screenRectangle2) : screenRectangle2;
	}
}
