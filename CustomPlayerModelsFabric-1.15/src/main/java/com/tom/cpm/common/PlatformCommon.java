package com.tom.cpm.common;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.ai.attributes.IAttribute;

import com.tom.cpm.shared.util.Log;

public class PlatformCommon {
	public static List<IAttribute> getReachAttr() {
		try {
			Class<?> clz = Class.forName("com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes");
			IAttribute a1 = (IAttribute) clz.getDeclaredField("REACH").get(null);
			IAttribute a2 = (IAttribute) clz.getDeclaredField("ATTACK_RANGE").get(null);
			Log.info("Loaded Reach Entity Attributes scaler");
			return Arrays.asList(a1, a2);
		} catch (Throwable e) {
		}
		return null;
	}
}
