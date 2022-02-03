package com.tom.cpm.mixin.of;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.texture.AbstractTexture;
import net.optifine.shaders.MultiTexID;

import com.tom.cpm.client.optifine.proxy.TextureOF;

@Mixin(AbstractTexture.class)
public class AbstractTextureMixin_OF implements TextureOF {

	@Shadow public MultiTexID multiTex;

	@Override
	public MultiTexID cpm$multiTex() {
		return multiTex;
	}

	@Override
	public void cpm$copyMultiTex(MultiTexID mt) {
		if(multiTex != null && mt != null) {
			multiTex.norm = mt.norm;
			multiTex.spec = mt.spec;
		}
	}
}
