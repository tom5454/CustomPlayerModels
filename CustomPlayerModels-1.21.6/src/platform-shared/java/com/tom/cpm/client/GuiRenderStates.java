package com.tom.cpm.client;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class GuiRenderStates {
	public record Colored4RectangleRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int x0, int y0, int x1, int y1,
			int topLeft, int topRight, int bottomLeft, int bottomRight, @Nullable ScreenRectangle scissorArea
			) implements GuiElementRenderState {

		@Override
		public void buildVertices(VertexConsumer vertexConsumer, float f) {
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0(), f).setColor(this.topRight());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1(), f).setColor(this.topLeft());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1(), f).setColor(this.bottomLeft());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0(), f).setColor(this.bottomRight());
		}
	}

	public record ColoredRectangleFRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, float x0, float y0, float x1, float y1,
			int color, @Nullable ScreenRectangle scissorArea
			) implements GuiElementRenderState {

		@Override
		public void buildVertices(VertexConsumer vertexConsumer, float f) {
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0(), f).setColor(this.color());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1(), f).setColor(this.color());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1(), f).setColor(this.color());
			vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0(), f).setColor(this.color());
		}
	}
}
