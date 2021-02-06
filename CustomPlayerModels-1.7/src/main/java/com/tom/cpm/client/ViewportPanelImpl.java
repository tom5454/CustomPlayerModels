package com.tom.cpm.client;

import static org.lwjgl.opengl.GL11.*;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.player.EntityPlayer;
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
			TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), PaintImageCreator.createImage(), false, false);
		}
	};
	private Minecraft mc;
	private EntityOtherPlayerMP playerObj;
	public ViewportPanelImpl(ViewportPanel panel) {
		super(panel);
		mc = Minecraft.getMinecraft();
		playerObj = new FakePlayer();
	}

	private void renderSetup() {
		float pitch = (float) Math.asin(editor.look.y);
		float yaw = editor.look.getYaw();

		GL11.glPushMatrix();
		Box bounds = getBounds();
		GL11.glTranslatef(bounds.x + bounds.w / 2, bounds.y + bounds.h / 2, 50);
		//GL11.glTranslatef(editor.position.x, editor.position.y, editor.position.z);
		float scale = editor.camDist;
		GL11.glScalef((-scale), scale, 0.1f);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
		//GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
		//RenderHelper.enableStandardItemLighting();
		//GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef((float) Math.toDegrees(pitch), 1, 0, 0);
		GL11.glRotatef((float) Math.toDegrees(yaw), 0, 1, 0);
		GL11.glTranslatef(-editor.position.x, -editor.position.y, -editor.position.z);
		//glDisable(GL_SCISSOR_TEST);
		float f = 1.0f;
		glColor3f(f, f, f);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(770, 771);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	private void renderFinish() {
		//glEnable(GL_SCISSOR_TEST);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
		RenderHelper.disableStandardItemLighting();
	}

	private void renderBase() {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		glDisable(GL_CULL_FACE);

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

		glEnable(GL_CULL_FACE);
		glEnable(GL_TEXTURE_2D);

		mc.getTextureManager().bindTexture(new ResourceLocation("cpm", "textures/gui/base.png"));
		Render.drawTexturedCube(0, -1.001, 0, 1, 1, 1);
	}

	@Override
	public void render(float partialTicks, int mouseX, int mouseY) {
		renderSetup();
		if(editor.renderPaint) {
			GL11.glDisable(GL11.GL_LIGHTING);
		} else if(editor.renderBase) renderBase();

		RenderPlayer rp = (RenderPlayer) RenderManager.instance.entityRenderMap.get(EntityPlayer.class);
		if(editor.renderPaint) {
			if(mc.getTextureManager().getTexture(PAINT) == null)
				mc.getTextureManager().loadTexture(PAINT, PAINT_TEX);
			mc.renderEngine.bindTexture(PAINT);
		} else {
			if(editor.skinProvider != null)editor.skinProvider.bind();
			else mc.renderEngine.bindTexture(new ResourceLocation("textures/entity/steve.png"));
		}
		float scale = 1;//0.0625F
		GL11.glTranslatef(0.5f, 1.5f, 0.5f);
		GL11.glRotatef(90, 0, 1, 0);
		GL11.glScalef((-scale), -scale, scale);
		ModelBiped p = rp.modelBipedMain;
		editor.preRender();
		try {
			ClientProxy.mc.getPlayerRenderManager().bindModel(p, editor.definition, null);
			setupModel(p);
			if(!editor.applyAnim && editor.playerTpose) {
				p.bipedRightArm.rotateAngleZ = (float) Math.toRadians(90);
				p.bipedLeftArm.rotateAngleZ = (float) Math.toRadians(-90);
			}

			float lsa = 0.75f;
			float ls = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTicks() * 0.2f - 1.5f * (1.0F - partialTicks);

			editor.applyRenderPoseForAnim(pose -> {
				switch (pose) {
				case SLEEPING:
					GL11.glTranslated(0.0D, 1.501F, 0.0D);
					GL11.glRotatef(-90, 0, 0, 1);
					GL11.glRotatef(270.0F, 0, 1, 0);
					break;

				case SNEAKING:
					p.isSneak = true;
					p.setRotationAngles(0, 0, 0, 0, 0, 0.0625F, playerObj);
					break;

				case RIDING:
					p.isRiding = true;
					p.setRotationAngles(0, 0, 0, 0, 0, 0.0625F, playerObj);
					break;
				case CUSTOM:
				case DYING:
				case FALLING:
				case STANDING:
					break;

				case FLYING:
					break;

				case RUNNING:
					p.setRotationAngles(ls, 1f, 0, 0, 0, 0.0625F, playerObj);
					break;

				case SWIMMING:
					break;

				case WALKING:
					p.setRotationAngles(ls, lsa, 0, 0, 0, 0.0625F, playerObj);
					break;

				default:
					break;
				}
			});

			glDisable(GL_CULL_FACE);
			p.render(playerObj, 0, 0, 0, 0, 0, 0.0625F);//Mouse.getX() / 1920f, Mouse.getY() / 1080f
			glEnable(GL_CULL_FACE);
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

	private void setupModel(ModelBiped p) {
		p.isChild = false;
		p.bipedHeadwear.showModel = false;
		p.isSneak = false;
		p.isRiding = false;
	}
}
