package com.tom.cpm.client;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.util.Image;
import com.tom.cpm.client.MinecraftObject.Texture;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.Panel3d;
import com.tom.cpm.shared.gui.panel.Panel3d.Panel3dNative;
import com.tom.cpm.shared.model.render.RenderMode;

public class Panel3dImpl extends Panel3dNative {
	private Minecraft mc;

	public Panel3dImpl(Panel3d panel) {
		super(panel);
		mc = Minecraft.INSTANCE;
	}

	@Override
	public void render(float partialTicks) {
		ViewportCamera cam = panel.getCamera();
		float pitch = (float) Math.asin(cam.look.y);
		float yaw = cam.look.getYaw();

		GL11.glPushMatrix();
		try {
			RetroGL.resetLightColor();
			Box bounds = getBounds();
			Vec2i off = panel.getGui().getOffset();
			GL11.glTranslatef(off.x + bounds.w / 2, off.y + bounds.h / 2, 50);
			float scale = cam.camDist;
			GL11.glScalef((-scale), scale, 0.1f);
			GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef((float) Math.toDegrees(pitch), 1, 0, 0);
			GL11.glRotatef((float) Math.toDegrees(yaw), 0, 1, 0);
			GL11.glTranslatef(-cam.position.x, -cam.position.y, -cam.position.z);
			float f = 1.0f;
			RetroGL.color4f(f, f, f, 1);

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(770, 771);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_CULL_FACE);

			panel.render(new MatrixStack(), new VBuffers(RetroGL::buffer), partialTicks);
		} finally {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glPopMatrix();
		}
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes() {
		return getRenderTypes0(Texture.bound != null ? Texture.bound.getId() : null);
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes(String tex) {
		return getRenderTypes0(mc.renderEngine.getTexture("/assets/cpm/textures/gui/" + tex + ".png"));
	}

	@Override
	public Image takeScreenshot(Vec2i size) {
		GuiImpl gui = panel.getGui().getNativeGui();
		float multiplierX = this.mc.resolution.width / (float)gui.width;
		float multiplierY = this.mc.resolution.height / (float)gui.height;
		int width = (int) (multiplierX * size.x);
		int height = (int) (multiplierY * size.y);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(width * height * 3);
		GL11.glReadPixels((int) (multiplierX * renderPos.x), this.mc.resolution.height - height - ((int) (multiplierY * renderPos.y)), width, height, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
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
		return Mat4f.map(GL11.GL_MODELVIEW_MATRIX, GL11::glGetFloat);
	}

	@Override
	public Mat4f getProjection() {
		return Mat4f.map(GL11.GL_PROJECTION_MATRIX, GL11::glGetFloat);
	}
}
