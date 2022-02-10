package com.tom.cpm.client;

import java.util.OptionalDouble;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class CustomRenderTypes extends RenderLayer {
	public static final RenderLayer LINES_NO_NEPTH = of("cpm:lines_no_depth", VertexFormats.POSITION_COLOR, 1, 256, MultiPhaseParameters.builder()
			.lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
			.layering(VIEW_OFFSET_Z_LAYERING)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.target(ITEM_TARGET)
			.writeMaskState(ALL_MASK)
			.depthTest(ALWAYS_DEPTH_TEST)
			.build(false));
	public static final RenderLayer ENTITY_COLOR = getEntityTranslucent(new Identifier("cpm:textures/white.png"));
	public static final RenderLayer ENTITY_COLOR_EYES = getEyes(new Identifier("cpm:textures/white.png"));

	public CustomRenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static RenderLayer getLinesNoDepth() {
		return LINES_NO_NEPTH;
	}

	public static RenderLayer getEntityColorTranslucentCull() {
		return ENTITY_COLOR;
	}

	public static RenderLayer getEntityColorEyes() {
		return ENTITY_COLOR_EYES;
	}

	public static RenderLayer getEntityTranslucentCullNoLight(Identifier texture) {
		MultiPhaseParameters multiPhaseParameters = MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(DISABLE_DIFFUSE_LIGHTING).alpha(ONE_TENTH_ALPHA).lightmap(DISABLE_LIGHTMAP).overlay(DISABLE_OVERLAY_COLOR).build(true);
		return of("cpm:entity_translucent_cull_nolight", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, true, multiPhaseParameters);
	}
}
