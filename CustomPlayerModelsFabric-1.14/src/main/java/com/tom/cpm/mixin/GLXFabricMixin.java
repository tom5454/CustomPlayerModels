package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GLX;

import com.tom.cpm.client.Platform;

@Mixin(GLX.class)
public class GLXFabricMixin {

	@Inject(at = @At("RETURN"), method = "glMultiTexCoord2f(IFF)V")
	private static void glMultiTexCoord2f(int p_glMultiTexCoord2f_0_, float p_glMultiTexCoord2f_1_, float p_glMultiTexCoord2f_2_, CallbackInfo cbi) {
		if (p_glMultiTexCoord2f_0_ == GLX.GL_TEXTURE1) {
			Platform.lastBrightnessX = p_glMultiTexCoord2f_1_;
			Platform.lastBrightnessY = p_glMultiTexCoord2f_2_;
		}
	}
}
