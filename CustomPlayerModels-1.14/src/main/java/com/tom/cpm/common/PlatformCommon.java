package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.PlayerEntity;

public class PlatformCommon {

	public static List<IAttribute> getReachAttr() {
		return Collections.singletonList(PlayerEntity.REACH_DISTANCE);
	}
}
