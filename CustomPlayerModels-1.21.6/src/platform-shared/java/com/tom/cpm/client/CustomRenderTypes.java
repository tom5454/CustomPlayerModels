package com.tom.cpm.client;

import static net.minecraft.client.renderer.RenderStateShard.*;
import static net.minecraft.client.renderer.RenderType.*;

import java.util.OptionalDouble;
import java.util.function.Supplier;

import net.irisshaders.batchedentityrendering.impl.BlendingStateHolder;
import net.irisshaders.batchedentityrendering.impl.TransparencyType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

public class CustomRenderTypes {
	public static final Supplier<RenderPipeline> EYES = Platform.registerPipeline(() -> {
		return RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
				.withLocation(ResourceLocation.tryBuild("cpm", "pipeline/eyes"))
				.withVertexShader("core/entity")
				.withFragmentShader("core/entity")
				.withShaderDefine("EMISSIVE")
				.withShaderDefine("NO_OVERLAY")
				.withShaderDefine("NO_CARDINAL_LIGHTING")
				.withSampler("Sampler0")
				.withBlend(BlendFunction.ADDITIVE)
				.withDepthWrite(false)
				.withVertexFormat(DefaultVertexFormat.NEW_ENTITY, Mode.QUADS)
				.build();
	});

	public static final Supplier<RenderPipeline> LINES = Platform.registerPipeline(() -> {
		return RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
				.withLocation(ResourceLocation.tryBuild("cpm", "pipeline/lines"))
				.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
				.build();
	});

	public static final RenderType ENTITY_COLOR = entityTranslucent(ResourceLocation.parse("textures/misc/white.png"));

	public static final RenderType ENTITY_COLOR_EYES = glowingEyes(ResourceLocation.parse("textures/misc/white.png"));

	public static RenderType entityColorTranslucent() {
		return ENTITY_COLOR;
	}

	public static RenderType entityColorEyes() {
		return ENTITY_COLOR_EYES;
	}

	public static RenderType linesNoDepth() {
		return create(
				"cpm:lines_no_depth",
				1536,
				LINES.get(),
				RenderType.CompositeState.builder().
				setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).
				setLayeringState(VIEW_OFFSET_Z_LAYERING).
				setOutputState(ITEM_ENTITY_TARGET).
				createCompositeState(false));
	}

	public static RenderType glowingEyes(ResourceLocation rl) {
		RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard(rl, false);
		RenderType rt = create(
				"eyes", 1536, false, true, EYES.get(), RenderType.CompositeState.builder().setTextureState(textureStateShard).createCompositeState(false)
				);
		if (ClientBase.irisLoaded)
			((BlendingStateHolder) rt).setTransparencyType(TransparencyType.DECAL);
		return rt;
	}
}
