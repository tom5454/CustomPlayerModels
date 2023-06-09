package com.tom.cpm.shared.model.render;

import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.SkinType;

public interface VanillaModelPart {
	default int getId(RenderedCube id) {
		return id.getCube().id;
	}
	String getName();
	PartValues getDefaultSize(SkinType skinType);

	default VanillaModelPart getCopyFrom() {
		return null;
	}

	default boolean needsPoseSetup() {
		return false;
	}
}