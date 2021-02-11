package com.tom.cpm.client;

import static org.lwjgl.opengl.GL11.*;

import java.io.IOException;
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
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.editor.gui.ViewportPanel;
import com.tom.cpm.shared.editor.gui.ViewportPanel.ViewportPanelNative;
import com.tom.cpm.shared.math.Box;
import com.tom.cpm.shared.util.PaintImageCreator;

public class ViewportPanelImpl extends ViewportPanelNative {
	private static final ResourceLocation PAINT = new ResourceLocation("cpm:paint_texture");
	private static final ITextureObject PAINT_TEX = new AbstractTexture() {

		@Override
		public void loadTexture(IResourceManager resourceManager) throws IOException {
			TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), PaintImageCreator.createImage().toBufferedImage(), false, false);
		}
	};
	private Minecraft mc;
	public ViewportPanelImpl(ViewportPanel panel) {
		super(panel);
		mc = Minecraft.getMinecraft();
	}

	private void renderSetup() {
		float pitch = (float) Math.asin(editor.look.y);
		float yaw = editor.look.getYaw();

		GlStateManager.enableAlpha();
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		Box bounds = getBounds();
		GlStateManager.translate(bounds.x + bounds.w / 2, bounds.y + bounds.h / 2, 50);
		//GlStateManager.translate(editor.position.x, editor.position.y, editor.position.z);
		float scale = editor.camDist;
		GlStateManager.scale((-scale), scale, 0.1f);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		//GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		//RenderHelper.enableStandardItemLighting();
		//GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate((float) Math.toDegrees(pitch), 1, 0, 0);
		GlStateManager.rotate((float) Math.toDegrees(yaw), 0, 1, 0);
		GlStateManager.translate(-editor.position.x, -editor.position.y, -editor.position.z);
		//glDisable(GL_SCISSOR_TEST);
		float f = 1.0f;
		glColor3f(f, f, f);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		GlStateManager.enableDepth();
	}

	private void renderFinish() {
		//glEnable(GL_SCISSOR_TEST);
		GlStateManager.disableDepth();
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	private void renderBase() {
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
	public void render(float partialTicks, int mouseX, int mouseY) {
		renderSetup();
		if(editor.renderPaint) {
			GlStateManager.disableLighting();
		} else if(editor.renderBase) renderBase();

		RenderPlayer rp = mc.getRenderManager().getSkinMap().get(editor.skinType == 1 ? "default" : "slim");
		if(editor.renderPaint) {
			if(mc.getTextureManager().getTexture(PAINT) == null)
				mc.getTextureManager().loadTexture(PAINT, PAINT_TEX);
			rp.bindTexture(PAINT);
		} else {
			if(editor.skinProvider != null)editor.skinProvider.bind();
			else rp.bindTexture(DefaultPlayerSkin.getDefaultSkin(mc.getSession().getProfile().getId()));
		}
		float scale = 1;//0.0625F
		GlStateManager.translate(0.5f, 1.5f, 0.5f);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.scale((-scale), -scale, scale);
		ModelPlayer p = rp.getMainModel();
		editor.preRender();
		try {
			ClientProxy.mc.getPlayerRenderManager().bindModel(p, null, editor.definition, null);
			setupModel(p);
			PlayerModelSetup.setRotationAngles(p, 0, 0, 0, 0, mc.gameSettings.mainHand);
			if(!editor.applyAnim && editor.playerTpose) {
				p.bipedRightArm.rotateAngleZ = (float) Math.toRadians(90);
				p.bipedLeftArm.rotateAngleZ = (float) Math.toRadians(-90);
			}

			float lsa = 0.75f;
			float ls = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTicks() * 0.2f - 1.5f * (1.0F - partialTicks);

			editor.applyRenderPoseForAnim(pose -> {
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

		renderFinish();

		if(editor.renderPaint) {
			FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
			GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
			colorUnderMouse = (((int)(buffer.get(0) * 255)) << 16) | (((int)(buffer.get(1) * 255)) << 8) | ((int)(buffer.get(2) * 255));
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
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
}
