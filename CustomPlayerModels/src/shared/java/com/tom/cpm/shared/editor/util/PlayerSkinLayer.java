package com.tom.cpm.shared.editor.util;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public enum PlayerSkinLayer {
	HAT,
	JACKET,
	LEFT_PANTS_LEG,
	RIGHT_PANTS_LEG,
	LEFT_SLEEVE,
	RIGHT_SLEEVE
	;
	public static final PlayerSkinLayer[] VALUES = values();
	private String lowerName;
	private PlayerSkinLayer() {
		lowerName = name().toLowerCase(Locale.ROOT);
	}

	public static PlayerSkinLayer getLayer(String name) {
		for (PlayerSkinLayer pl : VALUES) {
			if(name.equals(pl.lowerName))
				return pl;
		}
		return null;
	}

	public String getLowerName() {
		return lowerName;
	}

	public static PlayerSkinLayer getEnc(int v) {
		for (PlayerSkinLayer pl : VALUES) {
			if(v == (1 << pl.ordinal()))
				return pl;
		}
		return null;
	}

	public static int encode(Set<PlayerSkinLayer> layers) {
		int r = 0;
		for (PlayerSkinLayer l : layers) {
			r |= (1 << l.ordinal());
		}
		return r;
	}

	public static int encode(Map<PlayerSkinLayer, Boolean> layers) {
		int r = 0;
		for (Entry<PlayerSkinLayer, Boolean> e : layers.entrySet()) {
			if(e.getValue()) {
				r |= (1 << e.getKey().ordinal());
			}
		}
		return r;
	}
}
