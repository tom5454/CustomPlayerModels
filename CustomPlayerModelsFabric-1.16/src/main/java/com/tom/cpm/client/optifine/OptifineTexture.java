package com.tom.cpm.client.optifine;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.client.optifine.proxy.AbstractTextureOF;
import com.tom.cpm.shared.skin.TextureProvider;

public class OptifineTexture {

	public static void applyOptifineTexture(Identifier loc, TextureProvider skin) {
		if(CustomPlayerModelsClient.optifineLoaded) {
			AbstractTexture tex = MinecraftClient.getInstance().getTextureManager().getTexture(loc);
			DynTexture dt = (DynTexture) skin.texture.getNative();
			if(dt != null && tex != null)
				((AbstractTextureOF)dt).cpm$copyMultiTex(((AbstractTextureOF) tex).cpm$multiTex());
		}
	}
}
