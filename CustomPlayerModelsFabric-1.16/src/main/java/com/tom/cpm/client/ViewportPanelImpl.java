package com.tom.cpm.client;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import java.nio.FloatBuffer;
import java.util.function.Consumer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;

import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.Image;
import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.gui.ViewportPanelBase;
import com.tom.cpm.shared.gui.ViewportPanelBase.ViewportCamera;
import com.tom.cpm.shared.gui.ViewportPanelBase.ViewportPanelNative;

public class ViewportPanelImpl extends ViewportPanelNative {
	private MinecraftClient mc;
	private MatrixStack matrixstack;
	public ViewportPanelImpl(ViewportPanelBase panel) {
		super(panel);
		mc = MinecraftClient.getInstance();
	}

	@Override
	public void renderSetup() {
		ViewportCamera cam = panel.getCamera();
		float pitch = (float) Math.asin(cam.look.y);
		float yaw = cam.look.getYaw();

		RenderSystem.pushMatrix();
		Box bounds = getBounds();
		Vec2i off = panel.getGui().getOffset();
		RenderSystem.translatef(off.x + bounds.w / 2, off.y + bounds.h / 2, 600);
		RenderSystem.enableDepthTest();
		float scale = cam.camDist;
		RenderSystem.scalef((-scale), scale, 0.1f);
		matrixstack = new MatrixStack();
		matrixstack.scale(1, 1, 1);
		Quaternion quaternion = Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F);
		Quaternion quaternion1 = Vector3f.POSITIVE_X.getRadialQuaternion(pitch);
		quaternion.hamiltonProduct(quaternion1);
		matrixstack.multiply(quaternion);
		matrixstack.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(yaw));
		matrixstack.translate(-cam.position.x, -cam.position.y, -cam.position.z);
		RenderSystem.color4f(1, 1, 1, 1);
	}

	@Override
	public void renderFinish() {
		RenderSystem.disableDepthTest();
		RenderSystem.popMatrix();
		matrixstack = null;
	}

	@Override
	public void renderBase() {
		mc.getTextureManager().bindTexture(new Identifier("cpm", "textures/gui/area.png"));
		RenderSystem.disableCull();
		RenderSystem.enableAlphaTest();
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder t = tes.getBuffer();
		Matrix4f m = matrixstack.peek().getModel();
		t.begin(GL_QUADS, VertexFormats.POSITION_TEXTURE);
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
	public void render(float partialTicks) {
		PlayerEntityRenderer rp = mc.getEntityRenderDispatcher().modelRenderers.get(panel.getSkinType().getName());
		float scale = panel.getScale();//0.0625F
		matrixstack.translate(0.5f, 0, 0.5f);
		matrixstack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));
		matrixstack.scale((-scale), -scale, scale);
		matrixstack.translate(0, -1.5f, 0);
		PlayerEntityModel<AbstractClientPlayerEntity> p = rp.getModel();
		panel.preRender();
		try {
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindModel(p, mc.getBufferBuilders().getEntityVertexConsumers(), panel.getDefinition(), null, panel.getAnimMode());
			CallbackInfoReturnable<Identifier> cbi = new CallbackInfoReturnable<>(null, true);
			cbi.setReturnValue(DefaultSkinHelper.getTexture(mc.getSession().getProfile().getId()));
			CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(p, cbi);
			setupModel(p);
			int overlay = OverlayTexture.packUv(OverlayTexture.getU(0), OverlayTexture.getV(false));
			int light = 15 << 4 | 15 << 20;
			RenderLayer rt = !panel.applyLighting() ? CustomRenderTypes.getEntityTranslucentCullNoLight(cbi.getReturnValue()) : RenderLayer.getEntityTranslucentCull(cbi.getReturnValue());
			VertexConsumer buffer = mc.getBufferBuilders().getEntityVertexConsumers().getBuffer(rt);
			((RDH)CustomPlayerModelsClient.mc.getPlayerRenderManager().getHolder(p)).defaultType = rt;
			setHeldItem(Hand.RIGHT, ap -> p.rightArmPose = ap);
			setHeldItem(Hand.LEFT, ap -> p.leftArmPose = ap);
			PlayerModelSetup.setAngles(p, 0, 0, 0, 0, mc.options.mainArm, false);
			if(panel.isTpose()) {
				p.rightArm.roll = (float) Math.toRadians(90);
				p.leftArm.roll = (float) Math.toRadians(-90);
			}

			float lsa = 0.75f;
			float ls = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTicks() * 0.2f - 1.5f * (1.0F - partialTicks);

			panel.applyRenderPoseForAnim(pose -> {
				switch (pose) {
				case SLEEPING:
					matrixstack.translate(0.0D, 1.501F, 0.0D);
					matrixstack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(-90));
					matrixstack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270.0F));
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
					matrixstack.translate(0.0D, 1.0D, -0.5d);
					matrixstack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
					p.head.pitch = -(float)Math.PI / 4F;
					break;

				case RUNNING:
					PlayerModelSetup.setAngles(p, ls, 1f, 0, 0, mc.options.mainArm, false);
					break;

				case SWIMMING:
					PlayerModelSetup.setAngles(p, ls, lsa, 0, 0, mc.options.mainArm, true);
					matrixstack.translate(0.0D, 1.0D, -0.5d);
					matrixstack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));
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

			this.renderItem(p, getHandStack(Hand.RIGHT), ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND, Arm.RIGHT, matrixstack, mc.getBufferBuilders().getEntityVertexConsumers(), light);
			this.renderItem(p, getHandStack(Hand.LEFT), ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND, Arm.LEFT, matrixstack, mc.getBufferBuilders().getEntityVertexConsumers(), light);
		} finally {
			CustomPlayerModelsClient.mc.getPlayerRenderManager().unbindModel(p);
		}
	}

	private void setHeldItem(Hand hand, Consumer<ArmPose> pose) {
		switch (panel.getHeldItem(hand)) {
		case BLOCK:
		case SWORD:
			pose.accept(ArmPose.ITEM);
			break;
		case NONE:
		default:
			pose.accept(ArmPose.EMPTY);
			break;
		}
	}

	private ItemStack getHandStack(Hand hand) {
		switch (panel.getHeldItem(hand)) {
		case BLOCK:
			return new ItemStack(Blocks.STONE);
		case NONE:
			break;
		case SWORD:
			return new ItemStack(Items.NETHERITE_SWORD);
		default:
			break;
		}
		return ItemStack.EMPTY;
	}

	private void renderItem(PlayerEntityModel<AbstractClientPlayerEntity> model, ItemStack p_229135_2_, ModelTransformation.Mode p_229135_3_, Arm p_229135_4_, MatrixStack p_229135_5_, VertexConsumerProvider p_229135_6_, int p_229135_7_) {
		if (!p_229135_2_.isEmpty()) {
			p_229135_5_.push();
			model.setArmAngle(p_229135_4_, p_229135_5_);
			p_229135_5_.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90.0F));
			p_229135_5_.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
			boolean flag = p_229135_4_ == Arm.LEFT;
			p_229135_5_.translate((flag ? -1 : 1) / 16.0F, 0.125D, -0.625D);
			mc.getItemRenderer().renderItem(null, p_229135_2_, p_229135_3_, flag, p_229135_5_, p_229135_6_, null, p_229135_7_, OverlayTexture.DEFAULT_UV);
			p_229135_5_.pop();
		}
	}

	private void setupModel(PlayerEntityModel<AbstractClientPlayerEntity> p) {
		p.child = false;
		p.leftArmPose = ArmPose.EMPTY;
		p.rightArmPose = ArmPose.EMPTY;
		p.setVisible(true);
		p.helmet.visible = false;
		p.jacket.visible = false;
		p.leftPantLeg.visible = false;
		p.rightPantLeg.visible = false;
		p.leftSleeve.visible = false;
		p.rightSleeve.visible = false;
		p.sneaking = false;
		p.handSwingProgress = 0;
		p.riding = false;
	}

	@Override
	public int getColorUnderMouse() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
		GL11.glReadPixels((int) mc.mouse.getX(), mc.getWindow().getFramebufferHeight() - (int) mc.mouse.getY(), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
		int colorUnderMouse = (((int)(buffer.get(0) * 255)) << 16) | (((int)(buffer.get(1) * 255)) << 8) | ((int)(buffer.get(2) * 255));
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		return colorUnderMouse;
	}

	@Override
	public Image takeScreenshot(Vec2i size) {
		GuiImpl gui = (GuiImpl) panel.getGui();
		int dw = mc.getWindow().getWidth();
		int dh = mc.getWindow().getHeight();
		float multiplierX = dw / (float)gui.width;
		float multiplierY = dh / (float)gui.height;
		int width = (int) (multiplierX * size.x);
		int height = (int) (multiplierY * size.y);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(width * height * 3);
		GL11.glReadPixels((int) (multiplierX * renderPos.x), mc.getWindow().getFramebufferHeight() - height - (int) (multiplierY * renderPos.y), width, height, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
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
	public boolean canRenderHeldItem() {
		return true;
	}
}
