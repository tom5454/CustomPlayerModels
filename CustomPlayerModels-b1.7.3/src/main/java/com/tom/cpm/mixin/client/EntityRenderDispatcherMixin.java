package com.tom.cpm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.entity.EntityRenderDispatcher;

import com.tom.cpm.client.RetroGL;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor3f(FFF)V", remap = false), method = "method_1921(Lnet/minecraft/entity/Entity;F)V")
	public void onRenderC(float r, float g, float b) {
		RetroGL.lightColor3f(r, g, b);
	}
}
