package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.render.WorldRenderer;

import com.tom.cpm.client.optifine.proxy.WorldRendererOF;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin_OF implements WorldRendererOF {

	@Shadow public boolean renderOverlayEyes = false;

	@Override
	public boolean getRenderOverlayEyes() {
		return renderOverlayEyes;
	}
}
