package com.tom.cpm.shared.model;

import com.tom.cpm.shared.model.render.VanillaModelPart;

public enum RootModelType implements VanillaModelPart {
	;

	public static final RootModelType[] VALUES = values();

	@Override
	public String getName() {
		return name().toLowerCase();
	}

	@Override
	public PartValues getDefaultSize(SkinType skinType) {
		return null;
	}
}
