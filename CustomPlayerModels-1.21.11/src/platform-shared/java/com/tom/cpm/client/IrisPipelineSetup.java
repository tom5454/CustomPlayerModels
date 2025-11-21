package com.tom.cpm.client;

import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.programs.ShaderKey;

public class IrisPipelineSetup {

	public static void setup() {
		IrisPipelines.assignPipeline(CustomRenderTypes.EYES.get(), ShaderKey.ENTITIES_EYES);
	}
}
