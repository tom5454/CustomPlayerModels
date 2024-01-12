package com.tom.cpm.common;

import java.util.List;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.NeoForgeMod;

public class PlatformCommon {

	public static Biome.ClimateSettings getClimateSettings(Biome b) {
		return b.getModifiedClimateSettings();
	}

	public static List<Attribute> getReachAttr() {
		return List.of(NeoForgeMod.BLOCK_REACH.value(), NeoForgeMod.ENTITY_REACH.value());
	}

	public static List<Attribute> getStepHeightAttr() {
		return List.of(NeoForgeMod.STEP_HEIGHT.value());
	}
}
