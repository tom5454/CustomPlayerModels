package com.tom.cpm.client;

import java.util.function.Function;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import com.tom.cpm.core.CallbackReturnable;

public class ModelTexture {
	private CallbackReturnable<ResourceLocation> texture;
	private Function<ResourceLocation, RenderType> renderType;

	public ModelTexture(CallbackReturnable<ResourceLocation> texture,
			Function<ResourceLocation, RenderType> renderType) {
		this.texture = texture;
		this.renderType = renderType;
	}

	public ModelTexture(Function<ResourceLocation, RenderType> renderType) {
		this(new CallbackReturnable<>(), renderType);
	}

	public ModelTexture(ResourceLocation tex, Function<ResourceLocation, RenderType> renderType) {
		this(new CallbackReturnable<>(tex), renderType);
	}

	public ModelTexture(CallbackReturnable<ResourceLocation> texture) {
		this(texture, PlayerRenderManager.entity);
	}

	public ModelTexture() {
		this(PlayerRenderManager.entity);
	}

	public ModelTexture(ResourceLocation tex) {
		this(tex, PlayerRenderManager.entity);
	}

	public ResourceLocation getTexture() {
		return texture.getReturnValue();
	}

	public void setTexture(ResourceLocation texture) {
		this.texture.setReturnValue(texture);
	}

	public RenderType getRenderType() {
		return renderType.apply(getTexture());
	}
}
