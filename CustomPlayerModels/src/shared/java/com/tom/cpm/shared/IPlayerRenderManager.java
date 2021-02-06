package com.tom.cpm.shared;

import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.RenderedCube;

public interface IPlayerRenderManager {
	void cleanupRenderedCube(RenderedCube cube);
	ModelDefinitionLoader getLoader();
	AnimationEngine getAnimationEngine();
}
