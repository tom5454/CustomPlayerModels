package com.tom.cpm.mixin.of;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;
import net.optifine.entity.model.anim.ModelUpdater;

import com.tom.cpm.client.optifine.proxy.ModelPartOF;

@Mixin(net.minecraft.client.model.ModelPart.class)
public abstract class ModelPartOFMixin implements ModelPartOF {
	@Shadow private Identifier textureLocation = null;
	@Shadow private WorldRenderer renderGlobal;
	@Shadow private ModelUpdater modelUpdater;
	@SuppressWarnings("rawtypes")
	@Shadow public List spriteList;

	@Override
	public Identifier cpm$textureLocation() {
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
