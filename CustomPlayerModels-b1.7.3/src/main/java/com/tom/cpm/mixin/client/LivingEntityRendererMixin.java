package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.IBipedModel;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.client.RetroGL;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
	private static final String RENDER_METHOD = "render(Lnet/minecraft/entity/LivingEntity;DDDFF)V";
	private static final String MODEL_RENDER_METHOD = "Lnet/minecraft/client/render/entity/model/EntityModel;render(FFFFFF)V";

	private void onRenderPart(EntityModel model, int callLoc) {
		RetroGL.renderCallLoc = callLoc;
		Object r = this;
		if (r instanceof PlayerEntityRenderer && model instanceof BipedEntityModel) {
			PlayerEntityRenderer rp = (PlayerEntityRenderer) r;
			if(model == rp.field_295 || model == rp.field_296) {
				PlayerRenderManager m = CustomPlayerModelsClient.mc.getPlayerRenderManager();
				BipedEntityModel player = rp.bipedModel;
				BipedEntityModel armor = (BipedEntityModel) model;
				m.copyModelForArmor(player.body, armor.body);
				m.copyModelForArmor(player.head, armor.head);
				m.copyModelForArmor(player.leftArm, armor.leftArm);
				m.copyModelForArmor(player.leftLeg, armor.leftLeg);
				m.copyModelForArmor(player.rightArm, armor.rightArm);
				m.copyModelForArmor(player.rightLeg, armor.rightLeg);
				((IBipedModel) armor).cpm$setNoSetup(true);
			}
		}
	}

	@Inject(at = @At(value = "HEAD"), method = RENDER_METHOD)
	public void onRender(LivingEntity arg, double d, double e, double f, float g, float h, CallbackInfo cbi) {
		RetroGL.renderCallLoc = 0;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V", remap = false), method = RENDER_METHOD)
	public void onRenderC(float r, float g, float b, float a) {
		RetroGL.color4f(r, g, b, a);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 0), method = RENDER_METHOD)
	public void onRender0(EntityModel model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 0);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 1), method = RENDER_METHOD)
	public void onRender1(EntityModel model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 1);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 2), method = RENDER_METHOD)
	public void onRender2(EntityModel model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 2);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 3), method = RENDER_METHOD)
	public void onRender3(EntityModel model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 3);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 4), method = RENDER_METHOD)
	public void onRender4(EntityModel model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 4);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 5), method = RENDER_METHOD)
	public void onRender5(EntityModel model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 5);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}
}
