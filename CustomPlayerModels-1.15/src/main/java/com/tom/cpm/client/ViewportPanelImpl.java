package com.tom.cpm.client;

import java.nio.FloatBuffer;
import java.util.function.Consumer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.BipedModel.ArmPose;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.Image;
import com.tom.cpm.client.PlayerRenderManager.RDH;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.RootGroups;
import com.tom.cpm.shared.gui.ViewportPanelBase;
import com.tom.cpm.shared.gui.ViewportPanelBase.ViewportCamera;
import com.tom.cpm.shared.gui.ViewportPanelBase.ViewportPanelNative;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.util.PlayerModelLayer;

public class ViewportPanelImpl extends ViewportPanelNative {
	private Minecraft mc;
	private FakePlayer playerObj;
	private MatrixStack matrixstack;
	public ViewportPanelImpl(ViewportPanelBase panel) {
		super(panel);
		mc = Minecraft.getInstance();
		playerObj = new FakePlayer();
	}

	@Override
	public void renderSetup() {
		ViewportCamera cam = panel.getCamera();
		float pitch = (float) Math.asin(cam.look.y);
		float yaw = cam.look.getYaw();

		RenderSystem.pushMatrix();
		Box bounds = getBounds();
		Vec2i off = panel.getGui().getOffset();
		RenderSystem.translatef(off.x + bounds.w / 2, off.y + bounds.h / 2, -50);
		RenderSystem.enableDepthTest();
		float scale = cam.camDist;
		RenderSystem.scalef((-scale), scale, 0.1f);
		matrixstack = new MatrixStack();
		matrixstack.scale(1, 1, 1);
		Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
		Quaternion quaternion1 = Vector3f.XP.rotation(pitch);
		quaternion.multiply(quaternion1);
		matrixstack.rotate(quaternion);
		matrixstack.rotate(Vector3f.YP.rotation(yaw));
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
	public void render(float partialTicks) {
		PlayerRenderer rp = mc.getRenderManager().getSkinMap().get(panel.getSkinType().getName());
		float scale = 1;//0.0625F
		matrixstack.translate(0.5f, 1.5f, 0.5f);
		matrixstack.rotate(Vector3f.YP.rotationDegrees(90));
		matrixstack.scale((-scale), -scale, scale);
		PlayerModel<AbstractClientPlayerEntity> p = rp.getEntityModel();
		panel.preRender();
		try {
			ModelDefinition def = panel.getDefinition();
			ClientProxy.mc.getPlayerRenderManager().bindModel(p, mc.getRenderTypeBuffers().getBufferSource(), def, null, panel.getAnimMode());
			CallbackInfoReturnable<ResourceLocation> cbi = new CallbackInfoReturnable<>(null, true);
			cbi.setReturnValue(DefaultPlayerSkin.getDefaultSkin(playerObj.getUniqueID()));
			ClientProxy.mc.getPlayerRenderManager().bindSkin(p, cbi, TextureSheetType.SKIN);
			setupModel(p);
			int overlay = OverlayTexture.getPackedUV(OverlayTexture.getU(0), OverlayTexture.getV(false));
			int light = LightTexture.packLight(15, 15);
			RenderType rt = !panel.applyLighting() ? CustomRenderTypes.getEntityTranslucentCullNoLight(cbi.getReturnValue()) : RenderType.getEntityTranslucent(cbi.getReturnValue());
			IVertexBuilder buffer = mc.getRenderTypeBuffers().getBufferSource().getBuffer(rt);
			RDH rdh = (RDH) ClientProxy.mc.getPlayerRenderManager().getHolder(p);
			rdh.renderTypes.put(RenderMode.NORMAL, new NativeRenderType(rt, 0));
			setHeldItem(Hand.RIGHT, ap -> p.rightArmPose = ap);
			setHeldItem(Hand.LEFT, ap -> p.leftArmPose = ap);
			p.setRotationAngles(playerObj, 0, 0, 0, 0, 0);

			if(panel.isTpose()) {
				p.bipedRightArm.rotateAngleZ = (float) Math.toRadians(90);
				p.bipedLeftArm.rotateAngleZ = (float) Math.toRadians(-90);
			}

			float lsa = 0.75f;
			float ls = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTicks() * 0.2f - 1.5f * (1.0F - partialTicks);

			panel.applyRenderPoseForAnim(pose -> {
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
					matrixstack.translate(0.0D, 1.0D, -0.5d);
					matrixstack.rotate(Vector3f.XP.rotationDegrees(90));
					p.bipedHead.rotateAngleX = -(float)Math.PI / 4F;
					break;

				case RUNNING:
					p.setRotationAngles(playerObj, ls, 1f, 0, 0, 0);
					break;

				case SWIMMING:
					matrixstack.translate(0.0D, 1.0D, -0.5d);
					matrixstack.rotate(Vector3f.XP.rotationDegrees(90));
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

			if(def.hasRoot(RootModelType.CAPE) && panel.getArmorLayers().contains(PlayerModelLayer.CAPE)) {
				ClientProxy.mc.getPlayerRenderManager().bindSkin(p, cbi, TextureSheetType.CAPE);
				RenderType rtc = !panel.applyLighting() ? CustomRenderTypes.getEntityTranslucentCullNoLight(cbi.getReturnValue()) : RenderType.getEntityTranslucent(cbi.getReturnValue());
				rdh.renderTypes.put(RenderMode.NORMAL, new NativeRenderType(rtc, 0));
				buffer = mc.getRenderTypeBuffers().getBufferSource().getBuffer(rtc);
				ClientProxy.renderCape(matrixstack, buffer, light, null, partialTicks, p, def);
			}

			renderArmor(p, rdh, PlayerModelLayer.HELMET, light, def);
			renderArmor(p, rdh, PlayerModelLayer.BODY, light, def);
			renderArmor(p, rdh, PlayerModelLayer.LEGS, light, def);
			renderArmor(p, rdh, PlayerModelLayer.BOOTS, light, def);

			if(panel.getArmorLayers().contains(PlayerModelLayer.ELYTRA))renderElytra(p, rdh, light, def);

			mc.getRenderTypeBuffers().getBufferSource().getBuffer(rt);
			mc.getRenderTypeBuffers().getBufferSource().finish();

			this.renderItem(p, getHandStack(Hand.RIGHT), ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixstack, mc.getRenderTypeBuffers().getBufferSource(), light);
			this.renderItem(p, getHandStack(Hand.LEFT), ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixstack, mc.getRenderTypeBuffers().getBufferSource(), light);
		} finally {
			ClientProxy.mc.getPlayerRenderManager().unbindModel(p);
		}
	}

	private static final BipedModel<AbstractClientPlayerEntity> ARMOR_LEGS = new BipedModel<>(0.5F);
	private static final BipedModel<AbstractClientPlayerEntity> ARMOR_BODY = new BipedModel<>(1);
	private static final ElytraModel<AbstractClientPlayerEntity> modelElytra = new ElytraModel<>();
	private void renderArmor(PlayerModel<AbstractClientPlayerEntity> p, RDH rdh, PlayerModelLayer layer, int light, ModelDefinition def) {
		if(panel.getArmorLayers().contains(layer)) {
			BipedModel<AbstractClientPlayerEntity> model = layer == PlayerModelLayer.LEGS ? ARMOR_LEGS : ARMOR_BODY;
			String name = layer == PlayerModelLayer.LEGS ? "armor2" : "armor1";
			ClientProxy.mc.getPlayerRenderManager().bindModel(model, name, mc.getRenderTypeBuffers().getBufferSource(), def, null, panel.getAnimMode());
			p.setModelAttributes(model);
			model.bipedHead.copyModelAngles(p.bipedHead);
			model.bipedHeadwear.copyModelAngles(p.bipedHeadwear);
			model.bipedBody.copyModelAngles(p.bipedBody);
			model.bipedRightArm.copyModelAngles(p.bipedRightArm);
			model.bipedLeftArm.copyModelAngles(p.bipedLeftArm);
			model.bipedRightLeg.copyModelAngles(p.bipedRightLeg);
			model.bipedLeftLeg.copyModelAngles(p.bipedLeftLeg);
			CallbackInfoReturnable<ResourceLocation> cbi = new CallbackInfoReturnable<>(null, true, new ResourceLocation("cpm:textures/template/" + name + ".png"));
			ClientProxy.mc.getPlayerRenderManager().bindSkin(model, cbi, RootGroups.getGroup(layer.parts[0]).getTexSheet(layer.parts[0]));
			RenderType rtc = !panel.applyLighting() ? CustomRenderTypes.getEntityTranslucentCullNoLight(cbi.getReturnValue()) : RenderType.getEntityTranslucent(cbi.getReturnValue());
			rdh.renderTypes.put(RenderMode.NORMAL, new NativeRenderType(rtc, 0));
			setModelSlotVisible(model, layer);
			IVertexBuilder buffer = mc.getRenderTypeBuffers().getBufferSource().getBuffer(rtc);
			model.render(matrixstack, buffer, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
			ClientProxy.mc.getPlayerRenderManager().unbindModel(model);
		}
	}

	private void renderElytra(PlayerModel<AbstractClientPlayerEntity> p, RDH rdh, int light, ModelDefinition def) {
		if(def.hasRoot(RootModelType.ELYTRA_LEFT) || def.hasRoot(RootModelType.ELYTRA_RIGHT)) {
			ClientProxy.mc.getPlayerRenderManager().bindModel(modelElytra, mc.getRenderTypeBuffers().getBufferSource(), def, null, panel.getAnimMode());
			CallbackInfoReturnable<ResourceLocation> cbi = new CallbackInfoReturnable<>(null, true, new ResourceLocation("cpm:textures/template/elytra.png"));
			ClientProxy.mc.getPlayerRenderManager().bindSkin(modelElytra, cbi, TextureSheetType.ELYTRA);
			RenderType rtc = !panel.applyLighting() ? CustomRenderTypes.getEntityTranslucentCullNoLight(cbi.getReturnValue()) : RenderType.getEntityTranslucent(cbi.getReturnValue());
			rdh.renderTypes.put(RenderMode.NORMAL, new NativeRenderType(rtc, 0));
			matrixstack.push();
			matrixstack.translate(0.0D, 0.0D, 0.125D);
			p.copyModelAttributesTo(modelElytra);
			modelElytra.leftWing.rotationPointX = 5.0F;
			modelElytra.leftWing.rotationPointY = 0;
			modelElytra.leftWing.rotateAngleX = 0.2617994F;
			modelElytra.leftWing.rotateAngleZ = -0.2617994F;
			modelElytra.leftWing.rotateAngleY = 0;
			modelElytra.rightWing.rotationPointX = -modelElytra.leftWing.rotationPointX;
			modelElytra.rightWing.rotateAngleY = -modelElytra.leftWing.rotateAngleY;
			modelElytra.rightWing.rotationPointY = modelElytra.leftWing.rotationPointY;
			modelElytra.rightWing.rotateAngleX = modelElytra.leftWing.rotateAngleX;
			modelElytra.rightWing.rotateAngleZ = -modelElytra.leftWing.rotateAngleZ;
			IVertexBuilder ivertexbuilder = mc.getRenderTypeBuffers().getBufferSource().getBuffer(rtc);
			modelElytra.render(matrixstack, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			matrixstack.pop();
			ClientProxy.mc.getPlayerRenderManager().unbindModel(modelElytra);
		}
	}

	protected void setModelSlotVisible(BipedModel<AbstractClientPlayerEntity> modelIn, PlayerModelLayer slotIn) {
		modelIn.setVisible(false);
		switch(slotIn) {
		case HELMET:
			modelIn.bipedHead.showModel = true;
			modelIn.bipedHeadwear.showModel = true;
			break;
		case BODY:
			modelIn.bipedBody.showModel = true;
			modelIn.bipedRightArm.showModel = true;
			modelIn.bipedLeftArm.showModel = true;
			break;
		case LEGS:
			modelIn.bipedBody.showModel = true;
			modelIn.bipedRightLeg.showModel = true;
			modelIn.bipedLeftLeg.showModel = true;
			break;
		case BOOTS:
			modelIn.bipedRightLeg.showModel = true;
			modelIn.bipedLeftLeg.showModel = true;
			break;
		default:
			break;
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
			return new ItemStack(Items.DIAMOND_SWORD);
		default:
			break;
		}
		return ItemStack.EMPTY;
	}

	private void renderItem(PlayerModel<AbstractClientPlayerEntity> model, ItemStack p_229135_2_, ItemCameraTransforms.TransformType p_229135_3_, HandSide p_229135_4_, MatrixStack p_229135_5_, IRenderTypeBuffer p_229135_6_, int p_229135_7_) {
		if (!p_229135_2_.isEmpty()) {
			p_229135_5_.push();
			model.translateHand(p_229135_4_, p_229135_5_);
			p_229135_5_.rotate(Vector3f.XP.rotationDegrees(-90.0F));
			p_229135_5_.rotate(Vector3f.YP.rotationDegrees(180.0F));
			boolean flag = p_229135_4_ == HandSide.LEFT;
			p_229135_5_.translate((flag ? -1 : 1) / 16.0F, 0.125D, -0.625D);
			Minecraft.getInstance().getItemRenderer().renderItem(null, p_229135_2_, p_229135_3_, flag, p_229135_5_, p_229135_6_, null, p_229135_7_, OverlayTexture.NO_OVERLAY);
			p_229135_5_.pop();
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

	@Override
	public int getColorUnderMouse() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
		GL11.glReadPixels((int) mc.mouseHelper.getMouseX(), mc.getMainWindow().getFramebufferHeight() - (int) mc.mouseHelper.getMouseY(), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
		int colorUnderMouse = (((int)(buffer.get(0) * 255)) << 16) | (((int)(buffer.get(1) * 255)) << 8) | ((int)(buffer.get(2) * 255));
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		return colorUnderMouse;
	}

	@Override
	public Image takeScreenshot(Vec2i size) {
		GuiImpl gui = (GuiImpl) panel.getGui();
		int dw = mc.getMainWindow().getWidth();
		int dh = mc.getMainWindow().getHeight();
		float multiplierX = dw / (float)gui.width;
		float multiplierY = dh / (float)gui.height;
		int width = (int) (multiplierX * size.x);
		int height = (int) (multiplierY * size.y);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(width * height * 3);
		GL11.glReadPixels((int) (multiplierX * renderPos.x), mc.getMainWindow().getFramebufferHeight() - height - (int) (multiplierY * renderPos.y), width, height, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
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
}
