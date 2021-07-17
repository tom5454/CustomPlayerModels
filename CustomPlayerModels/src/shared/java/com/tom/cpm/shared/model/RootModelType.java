package com.tom.cpm.shared.model;

import com.tom.cpm.shared.model.render.VanillaModelPart;

public enum RootModelType implements VanillaModelPart {
	CAPE,
	ELYTRA_LEFT,
	ELYTRA_RIGHT,
	ARMOR_HELMET,
	ARMOR_BODY,
	ARMOR_LEFT_ARM,
	ARMOR_RIGHT_ARM,
	ARMOR_LEGGINGS_BODY,
	ARMOR_LEFT_LEG,
	ARMOR_RIGHT_LEG,
	ARMOR_LEFT_FOOT,
	ARMOR_RIGHT_FOOT,
	;

	public static final RootModelType[] VALUES = values();

	@Override
	public String getName() {
		return name().toLowerCase();
	}

	@Override
	public PartValues getDefaultSize(SkinType skinType) {
		return RootModelValues.getFor(this, skinType);
	}
}
