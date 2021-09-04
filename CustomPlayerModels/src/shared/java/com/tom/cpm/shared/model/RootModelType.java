package com.tom.cpm.shared.model;

import com.tom.cpm.shared.model.render.VanillaModelPart;

public enum RootModelType implements VanillaModelPart {
	CAPE(),
	ELYTRA_LEFT(),
	ELYTRA_RIGHT(),
	ARMOR_HELMET(PlayerModelParts.HEAD),
	ARMOR_BODY(PlayerModelParts.BODY),
	ARMOR_LEFT_ARM(PlayerModelParts.LEFT_ARM),
	ARMOR_RIGHT_ARM(PlayerModelParts.RIGHT_ARM),
	ARMOR_LEGGINGS_BODY(PlayerModelParts.BODY),
	ARMOR_LEFT_LEG(PlayerModelParts.LEFT_LEG),
	ARMOR_RIGHT_LEG(PlayerModelParts.RIGHT_LEG),
	ARMOR_LEFT_FOOT(PlayerModelParts.LEFT_LEG),
	ARMOR_RIGHT_FOOT(PlayerModelParts.RIGHT_LEG),
	;

	public static final RootModelType[] VALUES = values();
	private final VanillaModelPart copyFrom;

	private RootModelType() {
		this.copyFrom = null;
	}

	private RootModelType(VanillaModelPart copyFrom) {
		this.copyFrom = copyFrom;
	}

	@Override
	public String getName() {
		return name().toLowerCase();
	}

	@Override
	public PartValues getDefaultSize(SkinType skinType) {
		return RootModelValues.getFor(this, skinType);
	}

	@Override
	public VanillaModelPart getCopyFrom() {
		return copyFrom;
	}
}
