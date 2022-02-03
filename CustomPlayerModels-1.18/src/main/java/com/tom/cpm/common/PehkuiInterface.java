package com.tom.cpm.common;

import net.minecraft.server.level.ServerPlayer;

import com.tom.cpm.shared.network.NetHandler.ScalerInterface;

import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class PehkuiInterface implements ScalerInterface<ServerPlayer> {

	private void setValue(ScaleData scaleData, float newScale) {
		if(newScale < 0.01F || newScale > 10)newScale = 1;
		scaleData.setTargetScale(newScale);
		scaleData.setScale(newScale);
		scaleData.setScale(newScale);
		scaleData.tick();
		scaleData.onUpdate();
	}

	@Override
	public void setScale(ServerPlayer player, float newScale) {
		setValue(ScaleTypes.BASE.getScaleData(player), newScale);
	}

	@Override
	public void setEyeHeight(ServerPlayer player, float newScale) {
		setValue(ScaleTypes.EYE_HEIGHT.getScaleData(player), newScale);
	}

	@Override
	public void setHitbox(ServerPlayer player, float width, float height) {
		setValue(ScaleTypes.HITBOX_WIDTH.getScaleData(player), width);
		setValue(ScaleTypes.HITBOX_HEIGHT.getScaleData(player), height);
	}
}
