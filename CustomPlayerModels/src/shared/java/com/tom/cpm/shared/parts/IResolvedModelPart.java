package com.tom.cpm.shared.parts;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.util.TextureStitcher;

public interface IResolvedModelPart {
	default void preApply(ModelDefinition def) {}
	default void apply(ModelDefinition def) {}
	default void stitch(TextureStitcher stitcher) {}
}
