package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.entity.ai.attributes.Attribute;

import net.minecraftforge.common.ForgeMod;

public class PlatformCommon {

	public static List<Attribute> getReachAttr() {
		return Collections.singletonList(ForgeMod.REACH_DISTANCE.get());
	}
}
