package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.coderbot.batchedentityrendering.impl.TransparencyType;

import com.tom.cpm.client.optifine.proxy.BlendingStateHolderEx;

@Mixin(targets = {"net/minecraft/client/renderer/RenderType$Type"}, priority = 1100)
public class CompositeRenderTypeMixin_Iris implements BlendingStateHolderEx {

	@Unique
	private TransparencyType transparencyType;

	@Override
	public void setTransparencyType(TransparencyType paramTransparencyType) {
		this.transparencyType = paramTransparencyType;
	}
}
