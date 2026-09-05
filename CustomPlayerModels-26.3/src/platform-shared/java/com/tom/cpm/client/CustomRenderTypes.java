package com.tom.cpm.client;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

public class CustomRenderTypes {
	public static final Supplier<RenderPipeline> EYES = Platform.registerPipeline(() -> {
		return RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
				.withLocation(Identifier.tryBuild("cpm", "pipeline/eyes"))
				.withVertexShader("core/entity")
				.withFragmentShader("core/entity")
				.withShaderDefine("EMISSIVE")
				.withShaderDefine("NO_OVERLAY")
				.withShaderDefine("NO_CARDINAL_LIGHTING")
				.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
				.withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
				.withVertexBinding(0, DefaultVertexFormat.ENTITY)
				.withPrimitiveTopology(PrimitiveTopology.QUADS)
				.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
				.build();
	});

	public static final Supplier<RenderPipeline> LINES = Platform.registerPipeline(() -> {
		return RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
				.withLocation(Identifier.tryBuild("cpm", "pipeline/lines"))
				.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
				.withColorTargetState(ColorTargetState.DEFAULT)
				.build();
	});

	private static final RenderType LINES_NO_DEPTH = RenderType.create("cpm:lines_no_depth",
			RenderSetup.builder(LINES.get()).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.createRenderSetup());

	private static final Function<Identifier, RenderType> GLOWING_EYES = Util.memoize(identifier -> {
		final RenderSetup renderSetup9 = RenderSetup.builder(EYES.get())
				.withTexture("Sampler0", identifier)
				.sortOnUpload()
				.createRenderSetup();
		return RenderType.create("eyes", renderSetup9);
	});

	public static final RenderType ENTITY_COLOR = RenderTypes.entityTranslucent(Identifier.parse("cpm:textures/white.png"));

	public static final RenderType ENTITY_COLOR_EYES = glowingEyes(Identifier.parse("cpm:textures/white.png"));


	public static RenderType entityColorTranslucent() {
		return ENTITY_COLOR;
	}

	public static RenderType entityColorEyes() {
		return ENTITY_COLOR_EYES;
	}

	public static RenderType linesNoDepth() {
		return LINES_NO_DEPTH;
	}

	public static RenderType glowingEyes(Identifier rl) {
		return GLOWING_EYES.apply(rl);
	}
}
