package com.tom.cpm.shared;

import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;

public interface IPlayerRenderManager {
	ModelDefinitionLoader getLoader();
	AnimationEngine getAnimationEngine();
}
