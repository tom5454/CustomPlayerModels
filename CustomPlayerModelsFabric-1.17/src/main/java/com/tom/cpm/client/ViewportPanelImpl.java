package com.tom.cpm.client;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;

import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.editor.gui.ViewportPanel;
import com.tom.cpm.shared.editor.gui.ViewportPanel.ViewportPanelNative;
import com.tom.cpm.shared.math.Box;
import com.tom.cpm.shared.util.PaintImageCreator;

public class ViewportPanelImpl extends ViewportPanelNative {
	private static final Identifier PAINT = new Identifier("cpm:paint_texture");
	private static final AbstractTexture PAINT_TEX = new AbstractTexture() {
		private NativeImage dynamicTextureData;

		@Override
		public void load(ResourceManager manager) throws IOException {
			dynamicTextureData = MinecraftObject.createFromBufferedImage(PaintImageCreator.createImage());
			TextureUtil.allocate(this.getGlId(), dynamicTextureData.getWidth(), dynamicTextureData.getHeight());
			this.bindTexture();
			dynamicTextureData.upload(0, 0, 0, false);
		}

		@Override
		public void close() {
			if (this.dynamicTextureData != null) {
				this.dynamicTextureData.close();
				this.clearGlId();
				this.dynamicTextureData = null;
			}
		}
	};
	private MinecraftClient mc;
	private MatrixStack matrixstack;
	public ViewportPanelImpl(ViewportPanel panel) {
		super(panel);
		mc = MinecraftClient.getInstance();
	}

	private void renderSetup() {
		float pitch = (float) Math.asin(editor.look.y);
		float yaw = editor.look.getYaw();

		RenderSystem.pushMatrix();
		Box bounds = getBounds();
		RenderSystem.translatef(bounds.x + bounds.w / 2, bounds.y + bounds.h / 2, 500);
		RenderSystem.enableDepthTest();
		RenderSystem.enableAlphaTest();
		float scale = editor.camDist;
		RenderSystem.scalef((-scale), scale, 0.1f);
		matrixstack = new MatrixStack();
		matrixstack.scale(1, 1, 1);
		Quaternion quaternion = net.minecraft.util.math.Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F);
		Quaternion quaternion1 = net.minecraft.util.math.Vec3f.POSITIVE_X.getRadialQuaternion(pitch);
		quaternion.hamiltonProduct(quaternion1);
		matrixstack.multiply(quaternion);
		matrixstack.multiply(net.minecraft.util.math.Vec3f.POSITIVE_Y.getRadialQuaternion(yaw));
		matrixstack.translate(-editor.position.x, -editor.position.y, -editor.position.z);
		RenderSystem.color4f(1, 1, 1, 1);
	}

	private void renderFinish() {
		RenderSystem.disableDepthTest();
		RenderSystem.popMatrix();
		matrixstack = null;
	}

	private void renderBase() {
		mc.getTextureManager().bindTexture(new Identifier("cpm", "textures/gui/area.png"));
		RenderSystem.disableCull();
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder t = tes.getBuffer();
		Matrix4f m = matrixstack.peek().getModel();
		t.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		t.vertex(m, 4,  0,  4).texture(1, 1).next();
		t.vertex(m, 4,  0, -3).texture(0, 1).next();
		t.vertex(m, -3, 0, -3).texture(0, 0).next();
		t.vertex(m, -3, 0,  4).texture(1, 0).next();
		tes.draw();
		RenderSystem.enableCull();

		mc.getTextureManager().bindTexture(new Identifier("cpm", "textures/gui/base.png"));
		Render.drawTexturedCube(matrixstack, 0, -1.001f, 0, 1, 1, 1);
	}

	@Override
	public void render(float partialTicks, int mouseX, int mouseY) {
		renderSetup();
		if(!editor.renderPaint && editor.renderBase)renderBase();
		if(editor.renderPaint) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		}

		PlayerEntityRenderer rp = (PlayerEntityRenderer) mc.getEntityRenderDispatcher().modelRenderers.get(editor.skinType == 1 ? "default" : "slim");
		float scale = 1;//0.0625F
		matrixstack.translate(0.5f, 1.5f, 0.5f);
		matrixstack.multiply(net.minecraft.util.math.Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
		matrixstack.scale((-scale), -scale, scale);
		PlayerEntityModel<AbstractClientPlayerEntity> p = rp.getModel();
		editor.preRender();
		try {
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindModel(p, mc.getBufferBuilders().getEntityVertexConsumers(), editor.definition, null);
			CallbackInfoReturnable<Identifier> cbi = new CallbackInfoReturnable<>(null, true);
			cbi.setReturnValue(DefaultSkinHelper.getTexture(mc.getSession().getProfile().getId()));
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(p, cbi);
			if(editor.renderPaint) {
				if(mc.getTextureManager().getTexture(PAINT) == null)
					mc.getTextureManager().registerTexture(PAINT, PAINT_TEX);
			}
			setupModel(p);
			int overlay = OverlayTexture.packUv(OverlayTexture.getU(0), OverlayTexture.getV(false));
			int light = 15 << 4 | 15 << 20;
			RenderLayer rt = editor.renderPaint ? CustomRenderTypes.getEntityTranslucentCullNoLight(PAINT) : RenderLayer.getEntityTranslucentCull(cbi.getReturnValue());
			VertexConsumer buffer = mc.getBufferBuilders().getEntityVertexConsumers().getBuffer(rt);
			((RDH)CustomPlayerModelsClient.mc.getPlayerRenderManager().getHolder(p)).defaultType = rt;
			PlayerModelSetup.setAngles(p, 0, 0, 0, 0, mc.options.mainArm, false);

			if(!editor.applyAnim && editor.playerTpose) {
				p.rightArm.roll = (float) Math.toRadians(90);
				p.leftArm.roll = (float) Math.toRadians(-90);
			}

			float lsa = 0.75f;
			float ls = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTicks() * 0.2f - 1.5f * (1.0F - partialTicks);

			editor.applyRenderPoseForAnim(pose -> {
				switch (pose) {
				case SLEEPING:
					matrixstack.translate(0.0D, 1.501F, 0.0D);
					matrixstack.multiply(net.minecraft.util.math.Vec3f.POSITIVE_Z.getDegreesQuaternion(-90));
					matrixstack.multiply(net.minecraft.util.math.Vec3f.POSITIVE_Y.getDegreesQuaternion(270.0F));
					break;

				case SNEAKING:
					p.sneaking = true;
					PlayerModelSetup.setAngles(p, 0, 0, 0, 0, mc.options.mainArm, false);
					break;

				case RIDING:
					p.riding = true;
					PlayerModelSetup.setAngles(p, 0, 0, 0, 0, mc.options.mainArm, false);
					break;
				case CUSTOM:
				case DYING:
				case FALLING:
				case STANDING:
					break;

				case FLYING:
					break;

				case RUNNING:
					PlayerModelSetup.setAngles(p, ls, 1f, 0, 0, mc.options.mainArm, false);
					break;

				case SWIMMING:
					break;

				case WALKING:
					PlayerModelSetup.setAngles(p, ls, lsa, 0, 0, mc.options.mainArm, false);
					break;

				case SKULL_RENDER:
					p.setVisible(false);
					p.head.visible = true;
					matrixstack.translate(0.0D, 1.501F, 0.0D);
					break;

				default:
					break;
				}
			});

			p.render(matrixstack, buffer, light, overlay, 1, 1, 1, 1);
			mc.getBufferBuilders().getEntityVertexConsumers().getBuffer(rt);
			mc.getBufferBuilders().getEntityVertexConsumers().draw();
		} finally {
			CustomPlayerModelsClient.mc.getPlayerRenderManager().unbindModel(p);
		}

		renderFinish();

		if(editor.renderPaint) {
			FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
			GL11.glReadPixels((int) mc.mouse.getX(), mc.getWindow().getFramebufferHeight() - (int) mc.mouse.getY(), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
			colorUnderMouse = (((int)(buffer.get(0) * 255)) << 16) | (((int)(buffer.get(1) * 255)) << 8) | ((int)(buffer.get(2) * 255));
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		}
	}

	private void setupModel(PlayerEntityModel<AbstractClientPlayerEntity> p) {
		p.child = false;
		p.leftArmPose = ArmPose.EMPTY;
		p.rightArmPose = ArmPose.EMPTY;
		p.setVisible(true);
		p.hat.visible = false;
		p.jacket.visible = false;
		p.leftPants.visible = false;
		p.rightPants.visible = false;
		p.leftSleeve.visible = false;
		p.rightSleeve.visible = false;
		p.sneaking = false;
		p.handSwingProgress = 0;
		p.riding = false;
	}
}
