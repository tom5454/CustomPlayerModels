package com.tom.cpm.mixin.of;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.optifine.entity.model.anim.ModelUpdater;

import com.tom.cpm.client.optifine.proxy.ModelRendererOF;

@Mixin(ModelRenderer.class)
public abstract class ModelRendererOFMixin implements ModelRendererOF {
	@Shadow private ResourceLocation textureLocation = null;
	@Shadow private WorldRenderer renderGlobal;
	@Shadow private ModelUpdater modelUpdater;
	@SuppressWarnings("rawtypes")
	@Shadow public List spriteList;

	@Override
	public ResourceLocation cpm$textureLocation() {
		return textureLocation;
	}

	@Override
	public WorldRenderer cpm$renderGlobal() {
		return renderGlobal;
	}

	@Override
	public ModelUpdater cpm$modelUpdater() {
		return modelUpdater;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List cpm$spriteList() {
		return spriteList;
	}
}
