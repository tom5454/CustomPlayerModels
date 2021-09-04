package com.tom.cpm.core;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.model.TextureSheetType;

public class CPMASMClientHooks {

	public static ResourceLocation onGetEntityTexture(ResourceLocation rl, PlayerRenderer model) {
		ModelTexture tex = new ModelTexture(rl);
		ClientProxy.mc.getPlayerRenderManager().bindSkin(model.getModel(), tex, TextureSheetType.SKIN);
		return tex.getTexture();
	}
}
