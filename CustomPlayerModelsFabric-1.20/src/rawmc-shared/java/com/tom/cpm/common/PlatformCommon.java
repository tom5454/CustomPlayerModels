package com.tom.cpm.common;

import java.util.List;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.biome.Biome;

import com.tom.cpm.shared.util.Log;

public class PlatformCommon {

	public static Biome.ClimateSettings getClimateSettings(Biome b) {
		return b.climateSettings;
	}

	public static List<Attribute> getReachAttr() {
		try {
			Class<?> clz = Class.forName("com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes");
			Attribute a1 = (Attribute) clz.getDeclaredField("REACH").get(null);
			Attribute a2 = (Attribute) clz.getDeclaredField("ATTACK_RANGE").get(null);
			Log.info("Loaded Reach Entity Attributes scaler");
			return List.of(a1, a2);
		} catch (Throwable e) {
		}
		return null;
	}

	public static List<Attribute> getStepHeightAttr() {
		return null;
	}
}
