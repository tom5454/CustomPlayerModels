package com.tom.cpm.common;

import net.minecraft.entity.player.ServerPlayerEntity;

import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleType;

public class PehkuiInterface {

	public static void setScale(ServerPlayerEntity player, float newScale) {
		ScaleData scaleData = ScaleType.BASE.getScaleData(player);
		scaleData.setTargetScale(newScale);
		scaleData.setScale(newScale);
		scaleData.setScale(newScale);
		scaleData.tick();
		scaleData.onUpdate();
	}

}
