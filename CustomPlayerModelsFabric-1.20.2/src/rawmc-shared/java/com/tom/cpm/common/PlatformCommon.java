package com.tom.cpm.common;

import java.util.List;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.biome.Biome;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;

import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.shared.util.Log;

public class PlatformCommon {

	public static Biome.ClimateSettings getClimateSettings(Biome b) {
		return b.climateSettings;
	}

	public static List<Attribute> getReachAttr() {
		if (CustomPlayerModels.isModLoaded("reach-entity-attributes")) {
			Log.info("Loaded Reach Entity Attributes scaler");
			return List.of(ReachEntityAttributes.REACH, ReachEntityAttributes.ATTACK_RANGE);
		}
		return null;
	}

	public static List<Attribute> getStepHeightAttr() {
		return null;
	}
}
