package com.tom.cpm.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.irisshaders.batchedentityrendering.impl.BlendingStateHolder;
import net.irisshaders.batchedentityrendering.impl.TransparencyType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import com.tom.cpm.client.CustomRenderTypes;

@Mixin(CustomRenderTypes.class)
public class CustomRenderTypesMixin_Iris7 {

	@Overwrite(remap = false)
	public static RenderType glowingEyes(ResourceLocation rl) {
		RenderType rt = RenderType.eyes(rl);
		((BlendingStateHolder) rt).setTransparencyType(TransparencyType.DECAL);
		return rt;
	}
}
