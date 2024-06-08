package com.tom.cpm.client;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.EXTBlendFuncSeparate;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.DirectBuffer;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpm.client.MinecraftObject.Texture;
import com.tom.cpm.shared.retro.RetroGLAccess;

public class RetroGL implements RetroGLAccess<Integer> {
	public static final RetroTessellator tessellator = new RetroTessellator(Tessellator.instance);
	public static int renderCallLoc;
	public static final int GLINT_OVERLAY_LOC = 2;
	public static final int HURT_OVERLAY_LOC = 3;

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

	@Override
	public RenderStage texture(Integer tex) {
		return new RenderStage(true, true, true, () -> {
			bindTex(tex);
			if (renderCallLoc != RetroGL.GLINT_OVERLAY_LOC) {
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(770, 771);
			}
		}, () -> {
		}, GL11.GL_QUADS);
	}

	private static float lx, ly;

	@Override
	public RenderStage eyes(Integer tex) {
		return new RenderStage(true, true, true, () -> {
			if(renderCallLoc == RetroGL.HURT_OVERLAY_LOC)return;
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
			bindTex(tex);
		}, () -> {
			if(renderCallLoc == RetroGL.HURT_OVERLAY_LOC)return;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx, ly);
			glBlendFunc(770, 771, 1, 0);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
		}, GL11.GL_QUADS);
	}

	@Override
	public RenderStage linesNoDepth() {
		return lines;
	}

	@Override
	public RenderStage color() {
		return color;
	}

	private static void bindTex(Integer tex) {
		if(tex != null) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
		}
	}

	public static VertexBuffer buffer(NativeRenderType type) {
		RenderStage stage = type.getNativeType();
		return new RetroBuffer(tessellator, stage);
	}

	private static class RenderStage implements RetroLayer {
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

	public static Vec4f getColor() {
		return new Vec4f(red, green, blue, alpha);
	}

	private static float red, green, blue, alpha;

	public static void color4f(float r, float g, float b, float a) {
		red = r;
		green = g;
		blue = b;
		alpha = a;
		GL11.glColor4f(r, g, b, a);
	}

	@Override
	public Integer getDynTexture() {
		return Texture.bound != null ? Texture.bound.getId() : null;
	}

	private static final boolean blendExt, openGL14;
	static {
		ContextCapabilities contextcapabilities = GLContext.getCapabilities();
		blendExt = contextcapabilities.GL_EXT_blend_func_separate && !contextcapabilities.OpenGL14;
		openGL14 = contextcapabilities.OpenGL14 || contextcapabilities.GL_EXT_blend_func_separate;
	}

	public static void glBlendFunc(int p_148821_0_, int p_148821_1_, int p_148821_2_, int p_148821_3_) {
		if (openGL14) {
			if (blendExt)EXTBlendFuncSeparate.glBlendFuncSeparateEXT(p_148821_0_, p_148821_1_, p_148821_2_, p_148821_3_);
			else GL14.glBlendFuncSeparate(p_148821_0_, p_148821_1_, p_148821_2_, p_148821_3_);
		} else GL11.glBlendFunc(p_148821_0_, p_148821_1_);
	}
}
