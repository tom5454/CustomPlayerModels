package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer.PlayerHeadRenderInfo;
import net.minecraft.world.item.component.ResolvableProfile;

import com.tom.cpm.client.PlayerHeadRenderInfoAccess;

@Mixin(PlayerHeadRenderInfo.class)
public class PlayerHeadRenderInfoMixin implements PlayerHeadRenderInfoAccess {
	private ResolvableProfile cpm$profile;

	@Override
	public ResolvableProfile cpm$getProfile() {
		return cpm$profile;
	}

	@Override
	public void cpm$setProfile(ResolvableProfile cpm$profile) {
		this.cpm$profile = cpm$profile;
	}
}
