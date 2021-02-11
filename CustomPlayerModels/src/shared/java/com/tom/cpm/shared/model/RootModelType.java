package com.tom.cpm.shared.model;

import com.tom.cpm.shared.model.ModelRenderManager.ModelPart;

public enum RootModelType implements ModelPart {
	;

	public static final RootModelType[] VALUES = values();

	@Override
	public String getName() {
		return name().toLowerCase();
	}
}
