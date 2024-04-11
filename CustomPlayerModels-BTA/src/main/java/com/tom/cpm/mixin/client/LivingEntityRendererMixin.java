package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.LivingRenderer;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.model.ModelBase;
import net.minecraft.client.render.model.ModelBiped;
import net.minecraft.core.entity.EntityLiving;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.IBipedModel;
import com.tom.cpm.client.PlayerRenderManager;
import com.tom.cpm.client.RetroGL;

@Mixin(value = LivingRenderer.class, remap = false)
public class LivingEntityRendererMixin {
	private static final String RENDER_METHOD = "render(Lnet/minecraft/core/entity/EntityLiving;DDDFF)V";
	private static final String MODEL_RENDER_METHOD = "Lnet/minecraft/client/render/model/ModelBase;render(FFFFFF)V";

	private void onRenderPart(ModelBase model, int callLoc) {
		RetroGL.renderCallLoc = callLoc;
		Object r = this;
		if (r instanceof PlayerRenderer && model instanceof ModelBiped) {
			PlayerRenderer rp = (PlayerRenderer) r;
			if(model == rp.modelArmor || model == rp.modelArmorChestplate) {
				PlayerRenderManager m = CustomPlayerModelsClient.mc.getPlayerRenderManager();
				ModelBiped player = rp.modelBipedMain;
				ModelBiped armor = (ModelBiped) model;
				m.copyModelForArmor(player.bipedBody, armor.bipedBody);
				m.copyModelForArmor(player.bipedHead, armor.bipedHead);
				m.copyModelForArmor(player.bipedLeftArm, armor.bipedLeftArm);
				m.copyModelForArmor(player.bipedLeftLeg, armor.bipedLeftLeg);
				m.copyModelForArmor(player.bipedRightArm, armor.bipedRightArm);
				m.copyModelForArmor(player.bipedRightLeg, armor.bipedRightLeg);
				((IBipedModel) armor).cpm$setNoSetup(true);
			}
		}
	}

	@Inject(at = @At(value = "HEAD"), method = RENDER_METHOD)
	public void onRender(EntityLiving arg, double d, double e, double f, float g, float h, CallbackInfo cbi) {
		RetroGL.renderCallLoc = 0;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V", remap = false), method = RENDER_METHOD)
	public void onRenderC(float r, float g, float b, float a) {
		RetroGL.color4f(r, g, b, a);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 0), method = RENDER_METHOD)
	public void onRender0(ModelBase model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 0);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 1), method = RENDER_METHOD)
	public void onRender1(ModelBase model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 1);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 2), method = RENDER_METHOD)
	public void onRender2(ModelBase model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 2);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 3), method = RENDER_METHOD)
	public void onRender3(ModelBase model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 3);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 4), method = RENDER_METHOD)
	public void onRender4(ModelBase model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 4);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}

	@Redirect(at = @At(value = "INVOKE", target = MODEL_RENDER_METHOD, ordinal = 5), method = RENDER_METHOD)
	public void onRender5(ModelBase model, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
			float scale) {
		onRenderPart(model, 5);
		model.render(limbAngle, limbDistance, animationProgress, headYaw, headPitch, scale);
	}
}
