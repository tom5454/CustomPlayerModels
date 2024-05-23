package com.tom.cpm.mixin;

import java.io.IOException;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.tom.cpm.client.ClientBase.ShaderLoader;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.shared.util.Log;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixinFabric implements ShaderLoader {

	@Shadow
	abstract net.minecraft.client.renderer.ShaderInstance preloadShader(ResourceProvider arg, String string, VertexFormat vertexFormat) throws IOException;

	private ResourceProvider cpm$tmpRmngr;

	@Inject(at = @At("TAIL"), method = "reloadShaders(Lnet/minecraft/server/packs/resources/ResourceProvider;)V")
	public void onShadersLoading(ResourceProvider resourceManager, CallbackInfo cbi) {
		try {
			cpm$tmpRmngr = resourceManager;
			CustomPlayerModelsClient.INSTANCE.registerShaders(this);
		} finally {
			cpm$tmpRmngr = null;
		}
	}

	@Override
	public void cpm$registerShader(String name, VertexFormat vertexFormat, Consumer<ShaderInstance> finish) {
		try {
			finish.accept(preloadShader(cpm$tmpRmngr, name, vertexFormat));
		} catch (IOException e) {
			Log.error("Failed to load cpm '" + name + "' shader", e);
			finish.accept(null);
		}
	}
}
