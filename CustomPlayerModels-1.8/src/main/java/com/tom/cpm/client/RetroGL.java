package com.tom.cpm.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

import com.tom.cpl.render.DirectBuffer;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpl.render.VertexBuffer;

public class RetroGL {
	private static final RenderStage lines = new RenderStage(true, false, false, () -> {
		GlStateManager.disableDepth();
		GlStateManager.disableTexture2D();
	}, () -> {
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
	}, GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

	private static final RenderStage color = new RenderStage(true, false, false, () -> {
		GlStateManager.disableTexture2D();
	}, () -> {
		GlStateManager.enableTexture2D();
	}, GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

	private static final RenderStage texture = new RenderStage(true, true, true, () -> {
	}, () -> {
	}, GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

	private static float lx, ly;
	private static final RenderStage eyes = new RenderStage(true, true, true, () -> {
		lx = OpenGlHelper.lastBrightnessX;
		ly = OpenGlHelper.lastBrightnessY;
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.blendFunc(1, 1);
		GlStateManager.depthMask(true);
		int i = 0xF0;
		int j = i % 65536;
		int k = i / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
	}, () -> {
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx, ly);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.enableAlpha();
	}, GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

	public static RenderStage texture() {
		return texture;
	}

	public static RenderStage eyes() {
		return eyes;
	}

	public static RenderStage linesNoDepth() {
		return lines;
	}

	public static RenderStage color() {
		return color;
	}

	public static VertexBuffer buffer(NativeRenderType type) {
		RenderStage stage = type.getNativeType();
		return new RetroBuffer(Tessellator.getInstance(), stage);
	}

	private static class RenderStage {
		private boolean color, texture, normal;
		private Runnable begin, end;
		private int glMode;
		private VertexFormat format;

		public RenderStage(boolean color, boolean texture, boolean normal, Runnable begin, Runnable end, int glMode, VertexFormat format) {
			this.color = color;
			this.texture = texture;
			this.normal = normal;
			this.begin = begin;
			this.end = end;
			this.glMode = glMode;
			this.format = format;
		}

		public void begin(WorldRenderer buf) {
			begin.run();
			buf.begin(glMode, format);
		}

		public void end() {
			end.run();
		}
	}

	private static class RetroBuffer extends DirectBuffer<WorldRenderer> {
		private RenderStage stage;
		private Tessellator tes;

		public RetroBuffer(Tessellator tes, RenderStage stage) {
			super(tes.getWorldRenderer());
			this.tes = tes;
			this.stage = stage;
			stage.begin(buffer);
		}

		@Override
		protected void pushVertex(float x, float y, float z, float red, float green, float blue, float alpha,
				float u, float v, float nx, float ny, float nz) {
			buffer.pos(x, y, z);
			if(stage.texture)buffer.tex(u, v);
			if(stage.color)buffer.color(red, green, blue, alpha);
			if(stage.normal)buffer.normal(nx, ny, nz);
			buffer.endVertex();
		}

		@Override
		public void finish() {
			tes.draw();
			stage.end();
		}

	}
}
