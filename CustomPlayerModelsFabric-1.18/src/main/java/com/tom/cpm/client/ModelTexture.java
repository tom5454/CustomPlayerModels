package com.tom.cpm.client;

import java.util.function.Function;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class ModelTexture {
	private CallbackInfoReturnable<Identifier> texture;
	private Function<Identifier, RenderLayer> RenderLayer;

	public ModelTexture(CallbackInfoReturnable<Identifier> texture,
			Function<Identifier, RenderLayer> RenderLayer) {
		this.texture = texture;
		this.RenderLayer = RenderLayer;
	}

	public ModelTexture(Function<Identifier, RenderLayer> RenderLayer) {
		this(new CallbackInfoReturnable<>("", true), RenderLayer);
	}

	public ModelTexture(Identifier tex, Function<Identifier, RenderLayer> RenderLayer) {
		this(new CallbackInfoReturnable<>("", true, tex), RenderLayer);
	}

	public ModelTexture(CallbackInfoReturnable<Identifier> texture) {
		this(texture, PlayerRenderManager.entity);
	}

	public ModelTexture() {
		this(PlayerRenderManager.entity);
	}

	public ModelTexture(Identifier tex) {
		this(tex, PlayerRenderManager.entity);
	}

	public Identifier getTexture() {
		return texture.getReturnValue();
	}

	public void setTexture(Identifier texture) {
		this.texture.setReturnValue(texture);
	}

	public RenderLayer getRenderLayer() {
		return RenderLayer.apply(getTexture());
	}
}
