package com.tom.cpm.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

import com.tom.cpl.render.DirectBuffer;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpl.render.VertexBuffer;

public class RetroGL {
	public static final RetroTessellator tessellator = new RetroTessellator(Tessellator.instance);

	private static final RenderStage lines = new RenderStage(true, false, false, () -> {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}, () -> {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}, GL11.GL_LINES);

	private static final RenderStage color = new RenderStage(true, false, false, () -> {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}, () -> {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}, GL11.GL_QUADS);

	private static final RenderStage texture = new RenderStage(true, true, true, () -> {
	}, () -> {
	}, GL11.GL_QUADS);

	private static float lx, ly;
	private static final RenderStage eyes = new RenderStage(true, true, true, () -> {
		lx = OpenGlHelper.lastBrightnessX;
		ly = OpenGlHelper.lastBrightnessY;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		GL11.glDepthMask(true);
		int i = 0xF0;
		int j = i % 65536;
		int k = i / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
	}, () -> {
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx, ly);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}, GL11.GL_QUADS);

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
		return new RetroBuffer(tessellator, stage);
	}

	private static class RenderStage {
		private boolean color, texture, normal;
		private Runnable begin, end;
		private int glMode;

		public RenderStage(boolean color, boolean texture, boolean normal, Runnable begin, Runnable end, int glMode) {
			this.color = color;
			this.texture = texture;
			this.normal = normal;
			this.begin = begin;
			this.end = end;
			this.glMode = glMode;
		}

		public void begin(RetroTessellator buf) {
			begin.run();
			buf.begin(glMode);
		}

		public void end() {
			end.run();
		}
	}

	private static class RetroBuffer extends DirectBuffer<RetroTessellator> {
		private RenderStage stage;

		public RetroBuffer(RetroTessellator tes, RenderStage stage) {
			super(tes);
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
			buffer.draw();
			stage.end();
		}
	}

	public static class RetroTessellator {
		private Tessellator t;
		private double x, y, z;

		public RetroTessellator(Tessellator t) {
			this.t = t;
		}

		public RetroTessellator tex(float i, float j) {
			t.setTextureUV(i, j);
			return this;
		}

		public void endVertex() {
			t.addVertex(x, y, z);
		}

		public void begin(int p_78371_1_) {
			t.startDrawing(p_78371_1_);
		}

		public RetroTessellator pos(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		public RetroTessellator color(float p_78369_1_, float p_78369_2_, float p_78369_3_, float p_78369_4_) {
			t.setColorRGBA_F(p_78369_1_, p_78369_2_, p_78369_3_, p_78369_4_);
			return this;
		}

		public RetroTessellator normal(float p_78375_1_, float p_78375_2_, float p_78375_3_) {
			t.setNormal(p_78375_1_, p_78375_2_, p_78375_3_);
			return this;
		}

		public int draw() {
			return t.draw();
		}
	}
}
