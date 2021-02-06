package com.tom.cpm.shared.parts;

import java.util.Collections;
import java.util.List;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.skin.SkinProvider;

public interface IResolvedModelPart {
	default SkinProvider getSkin() { return null; }
	default List<RenderedCube> getModel() { return Collections.emptyList(); }
	default void apply(ModelDefinition def) {}
}
