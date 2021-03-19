package com.tom.cpm.client;

import java.util.OptionalDouble;

import net.minecraft.class_5944;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class CustomRenderTypes extends RenderLayer {
	public static final RenderLayer LINES_NO_NEPTH = of("cpm:lines_no_depth", VertexFormats.POSITION_COLOR, DrawMode.LINES, 256, MultiPhaseParameters.builder()
			.method_34578(field_29433)
			.lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
			.layering(VIEW_OFFSET_Z_LAYERING)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.target(ITEM_TARGET)
			.writeMaskState(ALL_MASK)
			.depthTest(ALWAYS_DEPTH_TEST)
			.cull(DISABLE_CULLING)
			.build(false));

	public static final RenderLayer ENTITY_COLOR = getEntityTranslucentCull(new Identifier("textures/misc/white.png"));

	public static class_5944 entityTranslucentCullNoLightShaderProgram;
	protected static final class_5942 entityTranslucentCullNoLightShader = new class_5942(() -> entityTranslucentCullNoLightShaderProgram);

	public CustomRenderTypes(String nameIn, VertexFormat formatIn, DrawMode drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static RenderLayer getLinesNoDepth() {
		return LINES_NO_NEPTH;
	}

	public static RenderLayer getEntityColorTranslucentCull() {
		return ENTITY_COLOR;
	}

	public static RenderLayer getEntityTranslucentCullNoLight(Identifier texture) {
		MultiPhaseParameters multiPhaseParameters = MultiPhaseParameters.builder().method_34578(entityTranslucentCullNoLightShader).method_34577(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(DISABLE_LIGHTMAP).overlay(DISABLE_OVERLAY_COLOR).build(true);
		return of("cpm:entity_translucent_cull_nolight", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, DrawMode.QUADS, 256, true, true, multiPhaseParameters);
	}
}
