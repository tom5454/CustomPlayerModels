package com.tom.cpm.shared;

public enum PlatformFeature {
	EDITOR_HELD_ITEM,
	EDITOR_SUPPORTED,
	;

	public boolean isSupported() {
		return MinecraftCommonAccess.get().getSupportedFeatures().contains(this);
	}

	public static String getVersion() {
		try {
			return MinecraftCommonAccess.get().getPlatformVersionString();
		} catch (Throwable e) {
			return "Error: " + e.toString();
		}
	}
}
