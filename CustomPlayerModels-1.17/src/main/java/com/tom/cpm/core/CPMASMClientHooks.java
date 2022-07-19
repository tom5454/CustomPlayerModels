package com.tom.cpm.core;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.ModelTexture;
import com.tom.cpm.shared.model.TextureSheetType;

public class CPMASMClientHooks {

	public static ResourceLocation onGetEntityTexture(ResourceLocation rl, PlayerRenderer model) {
		ModelTexture tex = new ModelTexture(rl);
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model.getModel(), tex, TextureSheetType.SKIN);
		return tex.getTexture();
	}
}
