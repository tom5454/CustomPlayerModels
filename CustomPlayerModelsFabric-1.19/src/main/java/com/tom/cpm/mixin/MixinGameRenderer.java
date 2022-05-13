package com.tom.cpm.mixin;

import java.io.IOException;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.CustomPlayerModelsClient.ShaderLoader;
import com.tom.cpm.shared.util.Log;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements ShaderLoader {

	@Shadow
	abstract net.minecraft.client.render.Shader loadShader(ResourceFactory arg, String string, VertexFormat vertexFormat) throws IOException;

	private ResourceManager cpm$tmpRmngr;

	@Inject(at = @At("TAIL"), method = "loadShaders(Lnet/minecraft/resource/ResourceManager;)V")
	public void onShadersLoading(ResourceManager resourceManager, CallbackInfo cbi) {
		try {
			cpm$tmpRmngr = resourceManager;
			CustomPlayerModelsClient.INSTANCE.registerShaders(this);
		} finally {
			cpm$tmpRmngr = null;
		}
	}

	@Override
	public void cpm$registerShader(String name, VertexFormat vertexFormat, Consumer<Shader> finish) {
		try {
			finish.accept(loadShader(cpm$tmpRmngr, name, vertexFormat));
		} catch (IOException e) {
			Log.error("Failed to load cpm '" + name + "' shader", e);
			finish.accept(null);
		}
	}
}
