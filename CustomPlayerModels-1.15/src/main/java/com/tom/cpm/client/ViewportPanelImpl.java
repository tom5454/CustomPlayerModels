package com.tom.cpm.client;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel.ArmPose;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.editor.gui.ViewportPanel;
import com.tom.cpm.shared.editor.gui.ViewportPanel.ViewportPanelNative;
import com.tom.cpm.shared.math.Box;
import com.tom.cpm.shared.util.PaintImageCreator;

public class ViewportPanelImpl extends ViewportPanelNative {
	private static final ResourceLocation PAINT = new ResourceLocation("cpm:paint_texture");
	private static final Texture PAINT_TEX = new Tex();
	private static class Tex extends Texture implements AutoCloseable {
		private NativeImage dynamicTextureData;

		@Override
		public void loadTexture(IResourceManager manager) throws IOException {
			dynamicTextureData = MinecraftObject.createFromBufferedImage(PaintImageCreator.createImage());
			TextureUtil.prepareImage(this.getGlTextureId(), dynamicTextureData.getWidth(), dynamicTextureData.getHeight());
			this.bindTexture();
			dynamicTextureData.uploadTextureSub(0, 0, 0, false);
		}

		@Override
		public void close() {
			if (this.dynamicTextureData != null) {
				this.dynamicTextureData.close();
				this.deleteGlTexture();
				this.dynamicTextureData = null;
			}
		}
	}
	private Minecraft mc;
	private FakePlayer playerObj;
	private MatrixStack matrixstack;
	public ViewportPanelImpl(ViewportPanel panel) {
		super(panel);
		mc = Minecraft.getInstance();
		playerObj = new FakePlayer();
	}

	private void renderSetup() {
		float pitch = (float) Math.asin(editor.look.y);
		float yaw = editor.look.getYaw();

		RenderSystem.pushMatrix();
		Box bounds = getBounds();
		RenderSystem.translatef(bounds.x + bounds.w / 2, bounds.y + bounds.h / 2, -50);
		RenderSystem.enableDepthTest();
		float scale = editor.camDist;
		RenderSystem.scalef((-scale), scale, 0.1f);
		matrixstack = new MatrixStack();
		matrixstack.scale(1, 1, 1);
		Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
		Quaternion quaternion1 = Vector3f.XP.rotation(pitch);
		quaternion.multiply(quaternion1);
		matrixstack.rotate(quaternion);
		matrixstack.rotate(Vector3f.YP.rotation(yaw));
		matrixstack.translate(-editor.position.x, -editor.position.y, -editor.position.z);
		RenderSystem.color4f(1, 1, 1, 1);
	}

	private void renderFinish() {
		RenderSystem.disableDepthTest();
		RenderSystem.popMatrix();
		matrixstack = null;
	}

	private void renderBase() {
		//mc.getTextureManager().bindTexture(new ResourceLocation("cpm", "textures/gui/area.png"));
		//RenderSystem.disableCull();
		//Tessellator tes = Tessellator.getInstance();
		//BufferBuilder t = tes.getBuffer();
		RenderType rt = CustomRenderTypes.getTexCutout(new ResourceLocation("cpm", "textures/gui/area.png"));
		IVertexBuilder t = mc.getRenderTypeBuffers().getBufferSource().getBuffer(rt);
		Matrix4f m = matrixstack.getLast().getMatrix();
		t.pos(m, 4,  0,  4).tex(1, 1).endVertex();
		t.pos(m, 4,  0, -3).tex(0, 1).endVertex();
		t.pos(m, -3, 0, -3).tex(0, 0).endVertex();
		t.pos(m, -3, 0,  4).tex(1, 0).endVertex();
		//RenderSystem.enableCull();
		mc.getRenderTypeBuffers().getBufferSource().finish(rt);

		mc.getTextureManager().bindTexture(new ResourceLocation("cpm", "textures/gui/base.png"));
		Render.drawTexturedCube(matrixstack, 0, -1.001f, 0, 1, 1, 1);
	}

	@Override
	public void render(float partialTicks, int mouseX, int mouseY) {
		renderSetup();
		if(!editor.renderPaint && editor.renderBase)renderBase();
		if(editor.renderPaint) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		}

		PlayerRenderer rp = mc.getRenderManager().getSkinMap().get(editor.skinType == 1 ? "default" : "slim");
		float scale = 1;//0.0625F
		matrixstack.translate(0.5f, 1.5f, 0.5f);
		matrixstack.rotate(Vector3f.YP.rotationDegrees(90));
		matrixstack.scale((-scale), -scale, scale);
		PlayerModel<AbstractClientPlayerEntity> p = rp.getEntityModel();
		editor.preRender();
		try {
			ClientProxy.mc.getPlayerRenderManager().bindModel(p, mc.getRenderTypeBuffers().getBufferSource(), editor.definition, null, null);
			CallbackInfoReturnable<ResourceLocation> cbi = new CallbackInfoReturnable<>(null, true);
			cbi.setReturnValue(DefaultPlayerSkin.getDefaultSkin(playerObj.getUniqueID()));
			ClientProxy.mc.getPlayerRenderManager().bindSkin(p, cbi);
			if(editor.renderPaint) {
				if(mc.getTextureManager().getTexture(PAINT) == null)
					mc.getTextureManager().loadTexture(PAINT, PAINT_TEX);
			}
			setupModel(p);
			int overlay = OverlayTexture.getPackedUV(OverlayTexture.getU(0), OverlayTexture.getV(false));
			int light = LightTexture.packLight(15, 15);
			RenderType rt = editor.renderPaint ? CustomRenderTypes.getEntityTranslucentCullNoLight(PAINT) : RenderType.getEntityTranslucent(cbi.getReturnValue());
			IVertexBuilder buffer = mc.getRenderTypeBuffers().getBufferSource().getBuffer(rt);
			((RDH)ClientProxy.mc.getPlayerRenderManager().getHolder(p)).defaultType = rt;
			p.setRotationAngles(playerObj, 0, 0, 0, 0, 0);

			if(!editor.applyAnim && editor.playerTpose) {
				p.bipedRightArm.rotateAngleZ = (float) Math.toRadians(90);
				p.bipedLeftArm.rotateAngleZ = (float) Math.toRadians(-90);
			}

			float lsa = 0.75f;
			float ls = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTicks() * 0.2f - 1.5f * (1.0F - partialTicks);

			editor.applyRenderPoseForAnim(pose -> {
				switch (pose) {
				case SLEEPING:
					matrixstack.translate(0.0D, 1.501F, 0.0D);
					matrixstack.rotate(Vector3f.ZP.rotationDegrees(-90));
					matrixstack.rotate(Vector3f.YP.rotationDegrees(270.0F));
					break;

				case SNEAKING:
					p.isSneak = true;
					p.setRotationAngles(playerObj, 0, 0, 0, 0, 0);
					break;

				case RIDING:
					p.isSitting = true;
					p.setRotationAngles(playerObj, 0, 0, 0, 0, 0);
					break;
				case CUSTOM:
				case DYING:
				case FALLING:
				case STANDING:
					break;

				case FLYING:
					break;

				case RUNNING:
					p.setRotationAngles(playerObj, ls, 1f, 0, 0, 0);
					break;

				case SWIMMING:
					break;

				case WALKING:
					p.setRotationAngles(playerObj, ls, lsa, 0, 0, 0);
					break;

				case SKULL_RENDER:
					p.setVisible(false);
					p.bipedHead.showModel = true;
					matrixstack.translate(0.0D, 1.501F, 0.0D);
					break;

				default:
					break;
				}
			});

			p.render(matrixstack, buffer, light, overlay, 1, 1, 1, 1);
			mc.getRenderTypeBuffers().getBufferSource().getBuffer(rt);
			mc.getRenderTypeBuffers().getBufferSource().finish();
		} finally {
			ClientProxy.mc.getPlayerRenderManager().unbindModel(p);
		}

		renderFinish();

		if(editor.renderPaint) {
			FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
			GL11.glReadPixels((int) mc.mouseHelper.getMouseX(), mc.getMainWindow().getFramebufferHeight() - (int) mc.mouseHelper.getMouseY(), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
			colorUnderMouse = (((int)(buffer.get(0) * 255)) << 16) | (((int)(buffer.get(1) * 255)) << 8) | ((int)(buffer.get(2) * 255));
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		}
	}

	private void setupModel(PlayerModel<AbstractClientPlayerEntity> p) {
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
		p.isSitting = false;
	}
}
