package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.modificationstation.stationapi.impl.client.arsenic.renderer.render.ArsenicOverlayRenderer;

import com.tom.cpm.client.RetroGL;

@Mixin(ArsenicOverlayRenderer.class)
public class ArsenicOverlayRendererMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V", remap = false), method = "renderItem", remap = false)
	public void onRenderC(float r, float g, float b, float a) {
		RetroGL.lightColor4f(r, g, b, a);
	}
}
