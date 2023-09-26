package com.tom.cpm.client;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Mat4f;
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
	private MatrixStack matrixstack;
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

		RenderSystem.pushMatrix();
		try {
			RenderSystem.translatef(off.x + bounds.w / 2, off.y + bounds.h / 2, -50);
			RenderSystem.scalef(1.0F, 1.0F, -0.1F);
			matrixstack = new MatrixStack();
			matrixstack.translate(0.0D, 0.0D, 1000.0D);
			matrixstack.scale(size, size, size);
			Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
			Quaternion quaternion1 = Vector3f.XP.rotation(-pitch);
			quaternion.mul(quaternion1);
			matrixstack.mulPose(quaternion);

			matrixstack.mulPose(Vector3f.YP.rotation((float) (yaw + Math.PI)));
			matrixstack.translate(-cam.position.x, -cam.position.y, -cam.position.z);
			RenderSystem.enableDepthTest();

			IRenderTypeBuffer.Impl bufs = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
			int light = LightTexture.pack(15, 15);
			panel.render(new com.tom.cpl.math.MatrixStack(), new VBuffers(rt -> new VBuffer(bufs.getBuffer(rt.getNativeType()), light, OverlayTexture.NO_OVERLAY, matrixstack)), partialTicks);
			bufs.endBatch();
		} finally {
			RenderSystem.disableDepthTest();
			RenderSystem.popMatrix();
			matrixstack = null;
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
		int dw = mc.getWindow().getWidth();
		int dh = mc.getWindow().getHeight();
		float multiplierX = dw / (float)gui.width;
		float multiplierY = dh / (float)gui.height;
		int width = (int) (multiplierX * size.x);
		int height = (int) (multiplierY * size.y);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(width * height * 3);
		GL11.glReadPixels((int) (multiplierX * renderPos.x), mc.getWindow().getHeight() - height - (int) (multiplierY * renderPos.y), width, height, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
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
		return Mat4f.map(GL11.GL_MODELVIEW_MATRIX, GL11::glGetFloatv, matrixstack.last().pose(), Matrix4f::store);
	}

	@Override
	public Mat4f getProjection() {
		return Mat4f.map(GL11.GL_PROJECTION_MATRIX, GL11::glGetFloatv);
	}
}
