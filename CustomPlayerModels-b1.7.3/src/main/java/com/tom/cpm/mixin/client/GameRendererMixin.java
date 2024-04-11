package com.tom.cpm.mixin.client;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.class_555;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;

import com.tom.cpm.client.IGameOptions;
import com.tom.cpm.client.PlayerProfile;
import com.tom.cpm.client.RetroGL;

@Mixin(class_555.class)
public class GameRendererMixin {
	private @Shadow Minecraft field_2349;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V"), method = "method_1844(F)V")
	public void onDrawScreenPre(float f, CallbackInfo cbi) {
		PlayerProfile.inGui = true;
		RetroGL.resetLightColor();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V", shift = Shift.AFTER), method = "method_1844(F)V")
	public void onDrawScreenPost(float f, CallbackInfo cbi) {
		PlayerProfile.inGui = false;
	}

	@Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;pitch:F", ordinal = 1), method = "method_1851(F)V")
	public float getPitch(LivingEntity e) {
		boolean v = ((IGameOptions) field_2349.options).cpm$getThirdPerson2();
		return v ? e.pitch + 180 : e.pitch;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glRotatef(FFFF)V", ordinal = 6, remap = false), method = "method_1851(F)V")
	public void onRotateCam(float f, CallbackInfo cbi) {
		boolean v = ((IGameOptions) field_2349.options).cpm$getThirdPerson2();
		if (v) GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
	}
}
