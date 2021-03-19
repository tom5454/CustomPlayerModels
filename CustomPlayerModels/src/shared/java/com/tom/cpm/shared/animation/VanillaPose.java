package com.tom.cpm.shared.animation;

import com.tom.cpl.gui.IGui;

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
	GLOBAL,

	;
	private final String i18nKey;
	public static final VanillaPose[] VALUES = values();

	private VanillaPose() {
		i18nKey = "label.cpm.pose." + name().toLowerCase();
	}

	@Override
	public String getName(IGui gui, String display) {
		if(display == null)return gui.i18nFormat(i18nKey);
		return gui.i18nFormat("label.cpm.vanilla_anim", gui.i18nFormat(i18nKey), display);
	}
}
