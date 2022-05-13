package com.tom.cpm.client;

import java.util.OptionalDouble;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class CustomRenderTypes extends RenderLayer {
	public static final RenderLayer ENTITY_COLOR = getEntityTranslucent(new Identifier("textures/misc/white.png"));
	public static final RenderLayer LINES_NO_DEPTH = of("cpm:lines_no_depth", VertexFormats.LINES, DrawMode.LINES, 256, false, false, MultiPhaseParameters.builder().shader(LINES_SHADER).lineWidth(new LineWidth(OptionalDouble.empty())).layering(VIEW_OFFSET_Z_LAYERING).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_TARGET).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(ALWAYS_DEPTH_TEST).build(false));
	public static final RenderLayer ENTITY_COLOR_EYES = getEyes(new Identifier("textures/misc/white.png"));

	//public static net.minecraft.client.render.Shader entityTranslucentCullNoLightShaderProgram;
	//protected static final net.minecraft.client.render.RenderPhase.Shader entityTranslucentCullNoLightShader = new net.minecraft.client.render.RenderPhase.Shader(() -> entityTranslucentCullNoLightShaderProgram);

	public CustomRenderTypes(String nameIn, VertexFormat formatIn, DrawMode drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static RenderLayer getEntityColorTranslucent() {
		return ENTITY_COLOR;
	}

	public static RenderLayer getEntityColorEyes() {
		return ENTITY_COLOR_EYES;
	}

	public static RenderLayer getLinesNoDepth() {
		return LINES_NO_DEPTH;
	}
}
