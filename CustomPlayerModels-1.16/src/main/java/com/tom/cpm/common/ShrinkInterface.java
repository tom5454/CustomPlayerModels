package com.tom.cpm.common;

import net.gigabit101.shrink.api.ShrinkAPI;
import net.minecraft.entity.player.ServerPlayerEntity;

public class ShrinkInterface {

	public static void setScale(ServerPlayerEntity player, float newScale) {
		player.getCapability(ShrinkAPI.SHRINK_CAPABILITY).ifPresent(iShrinkProvider -> iShrinkProvider.setScale(newScale));
	}
}
