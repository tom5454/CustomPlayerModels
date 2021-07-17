package com.tom.cpm.shared.util;

import com.tom.cpm.shared.model.RootModelType;

public enum PlayerModelLayer {
	CAPE(RootModelType.CAPE),
	ELYTRA(RootModelType.ELYTRA_LEFT, RootModelType.ELYTRA_RIGHT),
	HELMET(RootModelType.ARMOR_HELMET),
	BODY(RootModelType.ARMOR_BODY, RootModelType.ARMOR_LEFT_ARM, RootModelType.ARMOR_RIGHT_ARM),
	LEGS(RootModelType.ARMOR_LEGGINGS_BODY, RootModelType.ARMOR_LEFT_LEG, RootModelType.ARMOR_RIGHT_LEG),
	BOOTS(RootModelType.ARMOR_LEFT_FOOT, RootModelType.ARMOR_RIGHT_FOOT),
	;
	public static final PlayerModelLayer[] VALUES = values();
	public static final PlayerModelLayer[] ARMOR = new PlayerModelLayer[] {HELMET, BODY, LEGS, BOOTS};
	public RootModelType[] parts;
	private PlayerModelLayer(RootModelType... parts) {
		this.parts = parts;
	}

	public static PlayerModelLayer getLayer(RootModelType type) {
		for (PlayerModelLayer armorLayer : VALUES) {
			for (RootModelType t : armorLayer.parts) {
				if(t == type)return armorLayer;
			}
		}
		return null;
	}
}
