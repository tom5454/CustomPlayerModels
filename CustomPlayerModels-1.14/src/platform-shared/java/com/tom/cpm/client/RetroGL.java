package com.tom.cpm.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.DirectBuffer;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.shared.retro.RetroGLAccess;

public class RetroGL implements RetroGLAccess<ResourceLocation> {
	private static final RenderStage lines = new RenderStage(true, false, false, () -> {
		GlStateManager.disableDepthTest();
		GlStateManager.disableTexture();
	}, () -> {
		GlStateManager.enableTexture();
		GlStateManager.enableDepthTest();
	}, GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

	private static final RenderStage color = new RenderStage(true, false, false, () -> {
		GlStateManager.disableTexture();
	}, () -> {
		GlStateManager.enableTexture();
	}, GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

	@Override
	public RenderStage texture(ResourceLocation rl) {
		return new RenderStage(true, true, true, () -> {
			bindTex(rl);
		}, () -> {
		}, GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
	}

	private static float lx, ly;
	@Override
	public RenderStage eyes(ResourceLocation rl) {
		return new RenderStage(true, true, true, () -> {
			lx = Platform.lastBrightnessX();
			ly = Platform.lastBrightnessY();
			GlStateManager.enableBlend();
			GlStateManager.disableAlphaTest();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			GlStateManager.depthMask(true);
			int i = 0xF0;
			int j = i % 65536;
			int k = i / 65536;
			GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, j, k);
			Minecraft.getInstance().gameRenderer.resetFogColor(true);
			bindTex(rl);
		}, () -> {
			Minecraft.getInstance().gameRenderer.resetFogColor(false);
			GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, lx, ly);
			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.enableAlphaTest();
		}, GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
	}

	@Override
	public RenderStage linesNoDepth() {
		return lines;
	}

	@Override
	public RenderStage color() {
		return color;
	}

	private static void bindTex(ResourceLocation tex) {
		if(tex != null)
			Minecraft.getInstance().getTextureManager().bind(tex);
	}

	public static VertexBuffer buffer(NativeRenderType type) {
		RenderStage stage = type.getNativeType();
		return new RetroBuffer(Tessellator.getInstance(), stage);
	}

	private static class RenderStage implements RetroLayer {
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

		public void begin(BufferBuilder buf) {
			begin.run();
			buf.begin(glMode, format);
		}

		public void end() {
			end.run();
		}
	}

	private static class RetroBuffer extends DirectBuffer<BufferBuilder> {
		private RenderStage stage;
		private Tessellator tes;

		public RetroBuffer(Tessellator tes, RenderStage stage) {
			super(tes.getBuilder());
			this.tes = tes;
			this.stage = stage;
			stage.begin(buffer);
		}

		@Override
		protected void pushVertex(float x, float y, float z, float red, float green, float blue, float alpha,
				float u, float v, float nx, float ny, float nz) {
			buffer.vertex(x, y, z);
			if(stage.texture)buffer.uv(u, v);
			if(stage.color)buffer.color(red, green, blue, alpha);
			if(stage.normal)buffer.normal(nx, ny, nz);
			buffer.endVertex();
		}

		@Override
		public void finish() {
			tes.end();
			stage.end();
		}

	}

	public static Vec4f getColor() {
		GlStateManager.Color c = GlStateManager.COLOR;
		return new Vec4f(val(c.r), val(c.g), val(c.b), val(c.a));
	}

	private static float val(float color) {
		return color == -1 ? 1 : color;
	}

	@Override
	public ResourceLocation getDynTexture() {
		return DynTexture.getBoundLoc();
	}
}
