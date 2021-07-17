package com.tom.cpm.shared;

public enum PlatformFeature {
	EDITOR_HELD_ITEM,
	RENDER_ARMOR,
	RENDER_ELYTRA,
	RENDER_CAPE,
	;

	public boolean isSupported() {
		return MinecraftCommonAccess.get().getSupportedFeatures().contains(this);
	}
}
