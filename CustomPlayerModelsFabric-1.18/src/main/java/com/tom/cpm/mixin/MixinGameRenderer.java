package com.tom.cpm.mixin;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;

import com.tom.cpm.client.CustomRenderTypes;
import com.tom.cpm.shared.util.Log;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

	@Shadow
	abstract net.minecraft.client.render.Shader loadShader(ResourceFactory arg, String string, VertexFormat vertexFormat) throws IOException;

	@Inject(at = @At("TAIL"), method = "loadShaders(Lnet/minecraft/resource/ResourceManager;)V")
	public void onShadersLoading(ResourceManager resourceManager, CallbackInfo cbi) {
		try {
			CustomRenderTypes.entityTranslucentCullNoLightShaderProgram = loadShader(resourceManager, "rendertype_entity_translucent_cull_no_light", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
		} catch (IOException e) {
			Log.error("Failed to load cpm paint shader", e);
			CustomRenderTypes.entityTranslucentCullNoLightShaderProgram = null;
		}
	}
}
