package com.tom.cpm.common;

import java.util.List;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.biome.Biome;

import net.minecraftforge.common.ForgeMod;

public class PlatformCommon {

	public static Biome.ClimateSettings getClimateSettings(Biome b) {
		return b.getModifiedClimateSettings();
	}

	public static List<Attribute> getReachAttr() {
		return List.of(ForgeMod.BLOCK_REACH.get(), ForgeMod.ENTITY_REACH.get());
	}

	public static List<Attribute> getStepHeightAttr() {
		return List.of(ForgeMod.STEP_HEIGHT_ADDITION.get());
	}
}
