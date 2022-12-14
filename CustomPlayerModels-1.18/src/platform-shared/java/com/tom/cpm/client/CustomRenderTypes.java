package com.tom.cpm.client;

import java.util.OptionalDouble;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

public class CustomRenderTypes extends RenderType {
	public static final RenderType ENTITY_COLOR = entityTranslucent(new ResourceLocation("textures/misc/white.png"));
	public static final RenderType LINES_NO_DEPTH = create("cpm:lines_no_depth", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL).setDepthTestState(NO_DEPTH_TEST).createCompositeState(false));
	public static final RenderType ENTITY_COLOR_EYES = eyes(new ResourceLocation("textures/misc/white.png"));

	/*public static ShaderInstance entityTranslucentCullNoLightShaderProgram;
	protected static final RenderStateShard.ShaderStateShard entityTranslucentCullNoLightShader = new RenderStateShard.ShaderStateShard(() -> entityTranslucentCullNoLightShaderProgram);*/

	public CustomRenderTypes(String nameIn, VertexFormat formatIn, Mode drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static RenderType entityColorTranslucent() {
		return ENTITY_COLOR;
	}

	public static RenderType entityColorEyes() {
		return ENTITY_COLOR_EYES;
	}

	public static RenderType linesNoDepth() {
		return LINES_NO_DEPTH;
	}
}
