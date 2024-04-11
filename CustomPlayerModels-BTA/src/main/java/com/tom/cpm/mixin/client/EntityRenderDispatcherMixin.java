package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.EntityRenderDispatcher;

import com.tom.cpm.client.RetroGL;

@Mixin(value = EntityRenderDispatcher.class, remap = false)
public class EntityRenderDispatcherMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor3f(FFF)V", remap = false), method = "renderEntity(Lnet/minecraft/core/entity/Entity;F)V")
	public void onRenderC(float r, float g, float b) {
		RetroGL.lightColor3f(r, g, b);
	}
}
