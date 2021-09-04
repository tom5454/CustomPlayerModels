package com.tom.cpm.client;

import java.util.OptionalDouble;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

public class CustomRenderTypes extends RenderType {
	public static final RenderType ENTITY_COLOR = entityTranslucentCull(new ResourceLocation("textures/misc/white.png"));
	public static final RenderType LINES_NO_DEPTH = create("cpm:lines_no_depth", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL).setDepthTestState(NO_DEPTH_TEST).createCompositeState(false));

	public static ShaderInstance entityTranslucentCullNoLightShaderProgram;
	protected static final RenderStateShard.ShaderStateShard entityTranslucentCullNoLightShader = new RenderStateShard.ShaderStateShard(() -> entityTranslucentCullNoLightShaderProgram);

	public CustomRenderTypes(String nameIn, VertexFormat formatIn, Mode drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static RenderType getEntityColorTranslucentCull() {
		return ENTITY_COLOR;
	}

	public static RenderType getEntityTranslucentCullNoLight(ResourceLocation texture) {
		RenderType.CompositeState multiPhaseParameters = RenderType.CompositeState.builder().setShaderState(entityTranslucentCullNoLightShader).setTextureState(new RenderStateShard.TextureStateShard(texture, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).createCompositeState(true);
		return create("cpm:entity_translucent_cull_nolight", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, true, multiPhaseParameters);
	}

	public static RenderType getLinesNoDepth() {
		return LINES_NO_DEPTH;
	}

	public static RenderType getTexCutout(ResourceLocation locationIn) {
		return create("cpm:cutout", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 131072, true, false, RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_CUTOUT_SHADER).setCullState(NO_CULL).setTextureState(new TextureStateShard(locationIn, false, false)).createCompositeState(true));
	}

	public static RenderType getTranslucent(ResourceLocation rl) {
		return create("cpm:translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
				.setCullState(NO_CULL).setTextureState(new TextureStateShard(rl, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setOutputState(TRANSLUCENT_TARGET).createCompositeState(true));
	}
}
