package com.tom.cpm.client;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

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
	private Minecraft mc;
	private Vec2i mouse;
	private Mat4f view, proj;

	public Panel3dImpl(Panel3d panel) {
		super(panel);
		mc = Minecraft.getInstance();
	}

	@Override
	public void render(float partialTicks) {
		GuiImpl gui = panel.getGui().getNativeGui();
		Vec2i mouse = super.getMouse();
		Vec2i off = panel.getGui().getOffset();
		this.mouse = new Vec2i(mouse.x - off.x, mouse.y - off.y);

		Box bounds = getBounds();
		gui.drawPip(this, bounds.w, bounds.h, State::new);
	}

	public static record State(Panel3dImpl impl, int x0, int y0, int x1, int y1, ScreenRectangle bounds, ScreenRectangle scissorArea) implements PictureInPictureRenderState {

		public State(Panel3dImpl impl, int x0, int y0, int x1, int y1, ScreenRectangle scissorArea) {
			this(impl, x0, y0, x1, y1, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea), scissorArea);
		}

		@Override
		public float scale() {
			return 1;
		}
	}

	public static class Renderer extends PictureInPictureRenderer<State> {

		public Renderer(BufferSource bufferSource) {
			super(bufferSource);
		}

		@Override
		public Class<State> getRenderStateClass() {
			return State.class;
		}

		@Override
		protected void renderToTexture(State pipState, PoseStack poseStack) {
			float partialTicks = 0f;
			Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
			var panel = pipState.impl().panel;
			GuiImpl gui = panel.getGui().getNativeGui();

			ViewportCamera cam = panel.getCamera();
			float pitch = (float) Math.asin(-cam.look.y);
			float yaw = cam.look.getYaw();

			float size = cam.camDist;

			Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
			Quaternionf quaternion1 = Axis.XP.rotation(pitch);
			quaternion.mul(quaternion1);

			int i = Minecraft.getInstance().getWindow().getGuiScale();
			int j = (pipState.x1() - pipState.x0()) * i;
			int k = (pipState.y1() - pipState.y0()) * i;
			var proj = new Matrix4f().setOrtho(0.0F, j, k, 0f, -1000.0F, 1000.0F);
			pipState.impl().proj = Mat4f.map(proj, Matrix4f::get);

			try {
				poseStack.pushPose();
				poseStack.mulPose(new Matrix4f().scaling(size, size, size));
				poseStack.mulPose(quaternion);

				poseStack.mulPose(Axis.YP.rotation((float) (yaw + Math.PI)));
				poseStack.translate(-cam.position.x, -cam.position.y, -cam.position.z);

				pipState.impl().view = Mat4f.map(poseStack.last().pose(), Matrix4f::get);

				int light = LightTexture.pack(15, 15);
				panel.render(new com.tom.cpl.math.MatrixStack(), new VBuffers(rt -> new VBuffer(bufferSource.getBuffer(rt.getNativeType()), light, OverlayTexture.NO_OVERLAY, poseStack)), partialTicks);
			} finally {
				poseStack.popPose();
			}
		}

		@Override
		protected float getTranslateY(int i, int j) {
			return i / 2.0F;
		}

		@Override
		protected String getTextureLabel() {
			return "cpm:panel3d";
		}
	}

	@Override
	public Vec2i get3dSize() {
		return new Vec2i(panel.getBounds().w, panel.getBounds().h);
	}

	@Override
	public Vec2i getMouse() {
		return mouse;
	}

	@Override
	public void draw3dOverlay() {
		Box bounds = getBounds();
		Vec2i ws = get3dSize();
		panel.getGui().drawTexture(bounds.x, bounds.y, bounds.w, bounds.h, 0, 0, 1, 1);
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes() {
		return getRenderTypes0(DynTexture.getBoundLoc());
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes(String texture) {
		return getRenderTypes0(ResourceLocation.tryBuild("cpm", "textures/gui/" + texture + ".png"));
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
		return view;
	}

	@Override
	public Mat4f getProjection() {
		return proj;
	}
}
