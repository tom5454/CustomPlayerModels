package com.tom.cpm.common;

import net.minecraft.world.level.biome.Biome;

public class PlatformCommon {

	public static Biome.ClimateSettings getClimateSettings(Biome b) {
		return b.getModifiedClimateSettings();
	}
}
