package com.tom.cpm.client;

import net.irisshaders.iris.pipeline.IrisPipelines;
import net.minecraft.client.renderer.RenderPipelines;

public class IrisPipelineSetup {

	public static void setup() {
		IrisPipelines.copyPipeline(RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, CustomRenderTypes.EYES.get());
	}
}
