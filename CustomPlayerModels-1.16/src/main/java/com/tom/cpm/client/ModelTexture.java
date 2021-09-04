package com.tom.cpm.client;

import java.util.function.Function;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;

public class ModelTexture {
	private CallbackInfoReturnable<ResourceLocation> texture;
	private Function<ResourceLocation, RenderType> renderType;

	public ModelTexture(CallbackInfoReturnable<ResourceLocation> texture,
			Function<ResourceLocation, RenderType> renderType) {
		this.texture = texture;
		this.renderType = renderType;
	}

	public ModelTexture(Function<ResourceLocation, RenderType> renderType) {
		this(new CallbackInfoReturnable<>("", true), renderType);
	}

	public ModelTexture(ResourceLocation tex, Function<ResourceLocation, RenderType> renderType) {
		this(new CallbackInfoReturnable<>("", true, tex), renderType);
	}

	public ModelTexture(CallbackInfoReturnable<ResourceLocation> texture) {
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
