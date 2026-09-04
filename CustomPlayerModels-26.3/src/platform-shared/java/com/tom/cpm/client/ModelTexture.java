package com.tom.cpm.client;

import java.util.function.Function;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.Identifier;

import com.tom.cpl.render.RenderTypeBuilder.TextureHandler;

public class ModelTexture implements TextureHandler<Identifier, SubmitProfile> {
	private CallbackInfoReturnable<Identifier> texture;
	private Function<Identifier, SubmitProfile> renderType;

	public ModelTexture(CallbackInfoReturnable<Identifier> texture,
			Function<Identifier, SubmitProfile> renderType) {
		this.texture = texture;
		this.renderType = renderType;
	}

	public ModelTexture(Function<Identifier, SubmitProfile> renderType) {
		this(new CallbackInfoReturnable<>(null, true), renderType);
	}

	public ModelTexture(Identifier tex, Function<Identifier, SubmitProfile> renderType) {
		this(new CallbackInfoReturnable<>(null, true, tex), renderType);
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

	@Override
	public Identifier getTexture() {
		return texture.getReturnValue();
	}

	@Override
	public void setTexture(Identifier texture) {
		this.texture.setReturnValue(texture);
	}

	@Override
	public SubmitProfile getRenderType() {
		return renderType.apply(getTexture());
	}
}
