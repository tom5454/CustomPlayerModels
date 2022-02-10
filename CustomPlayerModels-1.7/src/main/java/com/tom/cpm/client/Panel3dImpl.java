package com.tom.cpm.client;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.shared.editor.DisplayItem;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.Panel3d;
import com.tom.cpm.shared.gui.panel.Panel3d.Panel3dNative;
import com.tom.cpm.shared.model.render.RenderMode;

public class Panel3dImpl extends Panel3dNative {
	private Minecraft mc;

	public Panel3dImpl(Panel3d panel) {
		super(panel);
		mc = Minecraft.getMinecraft();
	}

	@Override
	public void render(float partialTicks) {
		ViewportCamera cam = panel.getCamera();
		float pitch = (float) Math.asin(cam.look.y);
		float yaw = cam.look.getYaw();

		GL11.glPushMatrix();
		try {
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
			RenderHelper.disableStandardItemLighting();
		}
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes() {
		return getRenderTypes(DynTexture.getBoundLoc());
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes(String tex) {
		return getRenderTypes(new ResourceLocation("cpm", "textures/gui/" + tex + ".png"));
	}

	public RenderTypes<RenderMode> getRenderTypes(ResourceLocation tex) {
		RenderTypes<RenderMode> renderTypes = new RenderTypes<>(RenderMode.class);
		renderTypes.put(RenderMode.NORMAL, new NativeRenderType(RetroGL.texture(tex), 0));
		renderTypes.put(RenderMode.DEFAULT, new NativeRenderType(RetroGL.texture(tex), 0));
		renderTypes.put(RenderMode.GLOW, new NativeRenderType(RetroGL.eyes(tex), 1));
		renderTypes.put(RenderMode.OUTLINE, new NativeRenderType(RetroGL.linesNoDepth(), 2));
		renderTypes.put(RenderMode.COLOR, new NativeRenderType(RetroGL.color(), 0));
		renderTypes.put(RenderMode.PAINT, new NativeRenderType(RetroGL.paint(tex), 0));
		renderTypes.put(RenderMode.COLOR_GLOW, new NativeRenderType(RetroGL.color(), 1));
		return renderTypes;
	}

	@Override
	public int getColorUnderMouse() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
		GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
		int colorUnderMouse = (((int)(buffer.get(0) * 255)) << 16) | (((int)(buffer.get(1) * 255)) << 8) | ((int)(buffer.get(2) * 255));
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		return colorUnderMouse;
	}

	@Override
	public Image takeScreenshot(Vec2i size) {
		GuiImpl gui = (GuiImpl) panel.getGui();
		float multiplierX = mc.displayWidth / (float)gui.width;
		float multiplierY = mc.displayHeight / (float)gui.height;
		int width = (int) (multiplierX * size.x);
		int height = (int) (multiplierY * size.y);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(width * height * 3);
		GL11.glReadPixels((int) (multiplierX * renderPos.x), mc.displayHeight - height - ((int) (multiplierY * renderPos.y)), width, height, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
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
	public void renderItem(MatrixStack stack, ItemSlot hand, DisplayItem item) {
	}
}
