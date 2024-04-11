package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.ItemRenderer;

import com.tom.cpm.client.RetroGL;

@Mixin(value = ItemRenderer.class, remap = false)
public class ItemRendererMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V", remap = false), method = "renderItemInFirstPerson(F)V")
	public void onRenderC(float r, float g, float b, float a) {
		RetroGL.lightColor4f(r, g, b, a);
	}
}
