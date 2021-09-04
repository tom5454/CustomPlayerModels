package com.tom.cpm.common;

import net.gigabit101.shrink.api.ShrinkAPI;
import net.minecraft.server.level.ServerPlayer;

public class ShrinkInterface {

	public static void setScale(ServerPlayer player, float newScale) {
		player.getCapability(ShrinkAPI.SHRINK_CAPABILITY).ifPresent(iShrinkProvider -> iShrinkProvider.setScale(newScale));
	}
}
