package com.tom.cpm.client.optifine;

import net.minecraft.resources.ResourceLocation;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.shared.skin.TextureProvider;

public class OptifineTexture {

	public static void applyOptifineTexture(ResourceLocation loc, TextureProvider skin) {
		if(ClientProxy.optifineLoaded) {
			/*AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(loc);
			DynTexture dt = (DynTexture) skin.texture.getNative();
			if(dt != null && tex != null)
				((TextureOF)dt.getDynTex()).cpm$copyMultiTex(((TextureOF) tex).cpm$multiTex());*/
		}
	}
}
