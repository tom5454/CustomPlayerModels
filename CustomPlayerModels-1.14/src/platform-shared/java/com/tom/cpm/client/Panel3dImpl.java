package com.tom.cpm.client;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.platform.GlStateManager;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.util.Image;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.Panel3d;
import com.tom.cpm.shared.gui.panel.Panel3d.Panel3dNative;
import com.tom.cpm.shared.model.render.RenderMode;

public class Panel3dImpl extends Panel3dNative {
	private Minecraft mc;

	public Panel3dImpl(Panel3d panel) {
		super(panel);
		mc = Minecraft.getInstance();
	}

	@Override
	@SuppressWarnings("deprecation")
	public void render(float partialTicks) {
		ViewportCamera cam = panel.getCamera();
		float pitch = (float) Math.asin(cam.look.y);
		float yaw = cam.look.getYaw();
		Box bounds = getBounds();
		Vec2i off = panel.getGui().getOffset();
		float size = cam.camDist;

		GlStateManager.enableAlphaTest();
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		try {
			GlStateManager.translatef(off.x + bounds.w / 2, off.y + bounds.h / 2, 50);
			float scale = cam.camDist;
			GlStateManager.scalef((-scale), scale, 0.1f);
			GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotatef((float) Math.toDegrees(pitch), 1, 0, 0);
			GlStateManager.rotatef((float) Math.toDegrees(yaw), 0, 1, 0);
			GlStateManager.translatef(-cam.position.x, -cam.position.y, -cam.position.z);
			float f = 1.0f;
			GlStateManager.color3f(f, f, f);

			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			GlStateManager.enableDepthTest();
			GlStateManager.disableCull();

			panel.render(new MatrixStack(), new VBuffers(RetroGL::buffer), partialTicks);
		} finally {
			GlStateManager.disableDepthTest();
			GlStateManager.popMatrix();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableTexture();
		}
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes() {
		return getRenderTypes0(DynTexture.getBoundLoc());
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes(String texture) {
		return getRenderTypes0(new ResourceLocation("cpm", "textures/gui/" + texture + ".png"));
	}

	@Override
	public Image takeScreenshot(Vec2i size) {
		GuiImpl gui = panel.getGui().getNativeGui();
		int dw = mc.window.getWidth();
		int dh = mc.window.getHeight();
		float multiplierX = dw / (float)gui.width;
		float multiplierY = dh / (float)gui.height;
		int width = (int) (multiplierX * size.x);
		int height = (int) (multiplierY * size.y);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(width * height * 3);
		GL11.glReadPixels((int) (multiplierX * renderPos.x), mc.window.getHeight() - height - (int) (multiplierY * renderPos.y), width, height, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
		Image img = new Image(width, height);
		for(int y = 0;y<height;y++) {
			for(int x = 0;x<width;x++) {
				float r = buffer.get((x + y * width) * 3);
				float g = buffer.get((x + y * width) * 3 + 1);
				float b = buffer.get((x + y * width) * 3 + 2);
				int color = 0xff000000 | (((int)(r * 255)) << 16) | (((int)(g * 255)) << 8) | ((int)(b * 255));
				img.setRGB(x, height - y - 1, color);
			}
		}
		Image rImg = new Image(size.x, size.y);
		rImg.draw(img, 0, 0, size.x, size.y);
		return rImg;
	}

	@Override
	public Mat4f getView() {
		return Mat4f.map(GL11.GL_MODELVIEW_MATRIX, GL11::glGetFloatv);
	}

	@Override
	public Mat4f getProjection() {
		return Mat4f.map(GL11.GL_PROJECTION_MATRIX, GL11::glGetFloatv);
	}
}
