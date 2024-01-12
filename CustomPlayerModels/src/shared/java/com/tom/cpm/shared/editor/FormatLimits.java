package com.tom.cpm.shared.editor;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;

public class FormatLimits {
	public static int getSizeLimit() {
		boolean exp = ModConfig.getCommonConfig().getBoolean(ConfigKeys.EDITOR_EXPERIMENTAL_EXPORT, false);
		return exp ? 1024 : 25;
	}

	public static int getVectorLimit() {
		boolean exp = ModConfig.getCommonConfig().getBoolean(ConfigKeys.EDITOR_EXPERIMENTAL_EXPORT, false);
		return exp ? 1024 : Vec3f.MAX_POS;
	}

	public static int getAnimLenLimit() {
		boolean exp = ModConfig.getCommonConfig().getBoolean(ConfigKeys.EDITOR_EXPERIMENTAL_EXPORT, false);
		return exp ? Integer.MAX_VALUE : Short.MAX_VALUE;
	}

	public static int getAnimSortLimitsMin() {
		boolean exp = ModConfig.getCommonConfig().getBoolean(ConfigKeys.EDITOR_EXPERIMENTAL_EXPORT, false);
		return exp ? Integer.MIN_VALUE : Byte.MIN_VALUE;
	}

	public static int getAnimSortLimitsMax() {
		boolean exp = ModConfig.getCommonConfig().getBoolean(ConfigKeys.EDITOR_EXPERIMENTAL_EXPORT, false);
		return exp ? Integer.MAX_VALUE : Byte.MAX_VALUE;
	}
}
