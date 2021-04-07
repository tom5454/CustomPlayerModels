package com.tom.cpm.client;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.gui.ViewportPanelBase;
import com.tom.cpm.shared.gui.ViewportPanelBase.ViewportCamera;
import com.tom.cpm.shared.gui.ViewportPanelBase.ViewportPanelNative;

public class ViewportPanelImpl extends ViewportPanelNative {
	private Minecraft mc;
	public ViewportPanelImpl(ViewportPanelBase panel) {
		super(panel);
		mc = Minecraft.getMinecraft();
	}

	@Override
	public void renderSetup() {
		ViewportCamera cam = panel.getCamera();
		float pitch = (float) Math.asin(cam.look.y);
		float yaw = cam.look.getYaw();

		GlStateManager.enableAlpha();
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		Box bounds = getBounds();
		Vec2i off = panel.getGui().getOffset();
		GlStateManager.translate(off.x + bounds.w / 2, off.y + bounds.h / 2, 50);
		//GlStateManager.translate(editor.position.x, editor.position.y, editor.position.z);
		float scale = cam.camDist;
		GlStateManager.scale((-scale), scale, 0.1f);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		//GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		//RenderHelper.enableStandardItemLighting();
		//GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate((float) Math.toDegrees(pitch), 1, 0, 0);
		GlStateManager.rotate((float) Math.toDegrees(yaw), 0, 1, 0);
		GlStateManager.translate(-cam.position.x, -cam.position.y, -cam.position.z);
		//glDisable(GL_SCISSOR_TEST);
		float f = 1.0f;
		glColor3f(f, f, f);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		GlStateManager.enableDepth();
	}

	@Override
	public void renderFinish() {
		//glEnable(GL_SCISSOR_TEST);
		GlStateManager.disableDepth();
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	@Override
	public void renderBase() {
		GlStateManager.disableTexture2D();
		GlStateManager.disableCull();
		//glDisable(GL_CULL_FACE);

		glBegin(GL_QUADS);
		glColor4f(1, 1, 1, 1);
		for (float x = -3; x <= 4; x += 1/4f) {
			glVertex3f(x - 0.01f, 0, -3);
			glVertex3f(x + 0.01f, 0, -3);
			glVertex3f(x + 0.01f, 0, 4);
			glVertex3f(x - 0.01f, 0, 4);
		}
		for (float y = -3; y <= 4; y += 1/4f) {
			glVertex3f(-3, 0, y + 0.01f);
			glVertex3f(-3, 0, y - 0.01f);
			glVertex3f( 4, 0, y - 0.01f);
			glVertex3f( 4, 0, y + 0.01f);
		}
		glEnd();

		//glEnable(GL_CULL_FACE);
		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();

		mc.getTextureManager().bindTexture(new ResourceLocation("cpm", "textures/gui/base.png"));
		Render.drawTexturedCube(0, -1.001, 0, 1, 1, 1);
	}

	@Override
	public void render(float partialTicks) {
		if(!panel.applyLighting()) {
			GlStateManager.disableLighting();
		}

		RenderPlayer rp = mc.getRenderManager().getSkinMap().get(panel.getSkinType().getName());
		float scale = 1;//0.0625F
		GlStateManager.translate(0.5f, 1.5f, 0.5f);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.scale((-scale), -scale, scale);
		ModelPlayer p = rp.getMainModel();
		panel.preRender();
		try {
			ClientProxy.mc.getPlayerRenderManager().bindModel(p, null, panel.getDefinition(), null, null);
			if(panel.getDefinition().getSkinOverride() != null)panel.getDefinition().getSkinOverride().bind();
			else rp.bindTexture(DefaultPlayerSkin.getDefaultSkin(mc.getSession().getProfile().getId()));
			setupModel(p);
			PlayerModelSetup.setRotationAngles(p, 0, 0, 0, 0, mc.gameSettings.mainHand);
			if(panel.isTpose()) {
				p.bipedRightArm.rotateAngleZ = (float) Math.toRadians(90);
				p.bipedLeftArm.rotateAngleZ = (float) Math.toRadians(-90);
			}

			float lsa = 0.75f;
			float ls = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTicks() * 0.2f - 1.5f * (1.0F - partialTicks);

			panel.applyRenderPoseForAnim(pose -> {
				switch (pose) {
				case SLEEPING:
					GlStateManager.translate(0.0D, 1.501F, 0.0D);
					GlStateManager.rotate(-90, 0, 0, 1);
					GlStateManager.rotate(270.0F, 0, 1, 0);
					break;

				case SNEAKING:
					p.isSneak = true;
					PlayerModelSetup.setRotationAngles(p, 0, 0, 0, 0, mc.gameSettings.mainHand);
					break;

				case RIDING:
					p.isRiding = true;
					PlayerModelSetup.setRotationAngles(p, 0, 0, 0, 0, mc.gameSettings.mainHand);
					break;
				case CUSTOM:
				case DYING:
				case FALLING:
				case STANDING:
					break;

				case FLYING:
					break;

				case RUNNING:
					PlayerModelSetup.setRotationAngles(p, ls, 1f, 0, 0, mc.gameSettings.mainHand);
					break;

				case SWIMMING:
					break;

				case WALKING:
					PlayerModelSetup.setRotationAngles(p, ls, lsa, 0, 0, mc.gameSettings.mainHand);
					break;

				case SKULL_RENDER:
					p.setVisible(false);
					p.bipedHead.showModel = true;
					GlStateManager.translate(0.0D, 1.501F, 0.0D);
					break;

				default:
					break;
				}
			});

			GlStateManager.disableCull();
			PlayerModelSetup.render(p);
			GlStateManager.enableCull();
		} finally {
			ClientProxy.mc.getPlayerRenderManager().unbindModel(p);
		}
	}

	private void setupModel(ModelPlayer p) {
		p.isChild = false;
		p.leftArmPose = ArmPose.EMPTY;
		p.rightArmPose = ArmPose.EMPTY;
		p.setVisible(true);
		p.bipedHeadwear.showModel = false;
		p.bipedBodyWear.showModel = false;
		p.bipedLeftLegwear.showModel = false;
		p.bipedRightLegwear.showModel = false;
		p.bipedLeftArmwear.showModel = false;
		p.bipedRightArmwear.showModel = false;
		p.isSneak = false;
		p.swingProgress = 0;
		p.isRiding = false;
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
		GL11.glReadPixels((int) (multiplierX * renderPos.x), (int) (multiplierY * renderPos.y), width, height, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for(int y = 0;y<height;y++) {
			for(int x = 0;x<width;x++) {
				float r = buffer.get((x + y * width) * 3);
				float g = buffer.get((x + y * width) * 3 + 1);
				float b = buffer.get((x + y * width) * 3 + 2);
				int color = 0xff000000 | (((int)(r * 255)) << 16) | (((int)(g * 255)) << 8) | ((int)(b * 255));
				img.setRGB(x, y, color);
			}
		}
		BufferedImage rImg = new BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gr = rImg.createGraphics();
		gr.drawImage(img, 0, 0, size.x, size.y, null);
		gr.dispose();
		return new Image(rImg);
	}
}
