package com.tom.cpm.shared.model.render;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectRenderer;

public interface IExtraRenderDefinition {
	void render(RedirectRenderer<?> renderer, MatrixStack stack, VBuffers buf, RenderTypes<RenderMode> renderTypes, RenderedCube cube, boolean doRenderElems);
}
