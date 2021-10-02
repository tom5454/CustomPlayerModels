package com.tom.cpm.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class CustomRenderTypes extends RenderLayer {

	public static final RenderLayer ENTITY_COLOR = getEntityTranslucentCull(new Identifier("textures/misc/white.png"));

	public static net.minecraft.client.render.Shader entityTranslucentCullNoLightShaderProgram;
	protected static final net.minecraft.client.render.RenderPhase.Shader entityTranslucentCullNoLightShader = new net.minecraft.client.render.RenderPhase.Shader(() -> entityTranslucentCullNoLightShaderProgram);

	public CustomRenderTypes(String nameIn, VertexFormat formatIn, DrawMode drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static RenderLayer getEntityColorTranslucentCull() {
		return ENTITY_COLOR;
	}

	public static RenderLayer getEntityTranslucentCullNoLight(Identifier texture) {
		MultiPhaseParameters multiPhaseParameters = MultiPhaseParameters.builder().shader(entityTranslucentCullNoLightShader).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(DISABLE_LIGHTMAP).overlay(DISABLE_OVERLAY_COLOR).build(true);
		return of("cpm:entity_translucent_cull_nolight", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, DrawMode.QUADS, 256, true, true, multiPhaseParameters);
	}
}
