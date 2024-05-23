package com.tom.cpm.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.DirectBuffer;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.shared.retro.RetroGLAccess;

public class RetroGL implements RetroGLAccess<ResourceLocation> {
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
			lx = OpenGlHelper.lastBrightnessX;
			ly = OpenGlHelper.lastBrightnessY;
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			GlStateManager.depthMask(true);
			int i = 0xF0;
			int j = i % 65536;
			int k = i / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
			Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
			bindTex(rl);
		}, () -> {
			Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx, ly);
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.enableAlpha();
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
			Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
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
			super(tes.getBuffer());
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

	private static Vec4f colorHold;
	public static Vec4f getColor() {
		if (colorHold != null)return colorHold;
		GlStateManager.Color c = GlStateManager.colorState;
		return new Vec4f(val(c.red), val(c.green), val(c.blue), val(c.alpha));
	}

	public static void colorHold() {
		colorHold = getColor();
	}

	public static void colorHoldEnd() {
		colorHold = null;
	}

	private static float val(float color) {
		return color == -1 ? 1 : color;
	}

	@Override
	public ResourceLocation getDynTexture() {
		return DynTexture.getBoundLoc();
	}
}
