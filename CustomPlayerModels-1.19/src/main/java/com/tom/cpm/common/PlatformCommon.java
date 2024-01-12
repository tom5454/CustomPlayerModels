package com.tom.cpm.common;

import java.util.List;

import net.minecraft.world.entity.ai.attributes.Attribute;

import net.minecraftforge.common.ForgeMod;

public class PlatformCommon {

	public static List<Attribute> getReachAttr() {
		return List.of(ForgeMod.REACH_DISTANCE.get(), ForgeMod.ATTACK_RANGE.get());
	}

	public static List<Attribute> getStepHeightAttr() {
		return List.of(ForgeMod.STEP_HEIGHT_ADDITION.get());
	}
}
