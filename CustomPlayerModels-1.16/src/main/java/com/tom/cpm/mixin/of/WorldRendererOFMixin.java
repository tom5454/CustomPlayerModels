package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.WorldRenderer;

import com.tom.cpm.client.optifine.proxy.WorldRendererOF;

@Mixin(WorldRenderer.class)
public class WorldRendererOFMixin implements WorldRendererOF {

	@Shadow public boolean renderOverlayEyes = false;

	@Override
	public boolean getRenderOverlayEyes() {
		return renderOverlayEyes;
	}
}
