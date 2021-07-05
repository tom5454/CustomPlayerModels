package com.tom.cpm.shared.model;

import com.tom.cpm.shared.model.render.VanillaModelPart;

public enum PlayerModelParts implements VanillaModelPart {
	HEAD,
	BODY,
	LEFT_ARM,
	RIGHT_ARM,
	LEFT_LEG,
	RIGHT_LEG,
	CUSTOM_PART
	;
	public static final PlayerModelParts[] VALUES = values();

	@Override
	public int getId(RenderedCube rc) {
		return ordinal();
	}

	@Override
	public String getName() {
		return name().toLowerCase();
	}

	@Override
	public PartValues getDefaultSize(SkinType skinType) {
		return PlayerPartValues.getFor(this, skinType);
	}
}