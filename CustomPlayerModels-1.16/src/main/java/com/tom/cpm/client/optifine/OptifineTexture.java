package com.tom.cpm.client.optifine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.util.ResourceLocation;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.client.optifine.proxy.TextureOF;
import com.tom.cpm.shared.skin.TextureProvider;

public class OptifineTexture {

	public static void applyOptifineTexture(ResourceLocation loc, TextureProvider skin) {
		if(ClientProxy.optifineLoaded) {
			Texture tex = Minecraft.getInstance().getTextureManager().getTexture(loc);
			DynTexture dt = (DynTexture) skin.texture.getNative();
			if(dt != null && tex != null)
				((TextureOF)dt.getDynTex()).cpm$copyMultiTex(((TextureOF) tex).cpm$multiTex());
		}
	}
}
