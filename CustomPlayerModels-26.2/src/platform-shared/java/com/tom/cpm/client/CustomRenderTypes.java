package com.tom.cpm.client;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

public class CustomRenderTypes {
	public static final Supplier<RenderPipeline> EYES = Platform.registerPipeline(() -> {
		return RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
				.withLocation(Identifier.tryBuild("cpm", "pipeline/eyes"))
				.withVertexShader("core/entity")
				.withFragmentShader("core/entity")
				.withShaderDefine("EMISSIVE")
				.withShaderDefine("NO_OVERLAY")
				.withShaderDefine("NO_CARDINAL_LIGHTING")
				.withSampler("Sampler0")
				.withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
				.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
				.withVertexFormat(DefaultVertexFormat.ENTITY, Mode.QUADS)
				.build();
	});

	public static final Supplier<RenderPipeline> LINES = Platform.registerPipeline(() -> {
		return RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
				.withLocation(Identifier.tryBuild("cpm", "pipeline/lines"))
				.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
				.build();
	});

	private static final RenderType LINES_NO_DEPTH = RenderType.create("cpm:lines_no_depth",
			RenderSetup.builder(LINES.get()).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET).createRenderSetup());

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
