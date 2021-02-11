package com.tom.cpm.shared.animation;

import java.util.function.Function;

public enum VanillaPose implements IPose {
	CUSTOM,
	STANDING,
	WALKING,
	RUNNING,
	SNEAKING,
	SWIMMING,
	FALLING,
	SLEEPING,
	RIDING,
	FLYING,
	DYING,
	SKULL_RENDER,

	;
	private final String i18nKey;
	public static final VanillaPose[] VALUES = values();

	private VanillaPose() {
		i18nKey = "label.cpm.pose." + name().toLowerCase();
	}

	@Override
	public String getName(Function<String, String> i18n) {
		return i18n.apply(i18nKey);
	}
}
