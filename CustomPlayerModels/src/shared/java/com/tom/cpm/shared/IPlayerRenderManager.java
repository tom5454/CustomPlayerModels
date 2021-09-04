package com.tom.cpm.shared;

import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectHolder;

public interface IPlayerRenderManager {
	AnimationEngine getAnimationEngine();
	RedirectHolder<?, ?, ?, ?> getHolderFor(Object model);
}
