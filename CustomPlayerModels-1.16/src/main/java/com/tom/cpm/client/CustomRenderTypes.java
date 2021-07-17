package com.tom.cpm.client;

import java.util.OptionalDouble;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

public class CustomRenderTypes extends RenderType {
	public static final RenderType LINES_NO_NEPTH = makeType("cpm:lines_no_depth", DefaultVertexFormats.POSITION_COLOR, 1, 256, RenderType.State.getBuilder().line(new RenderState.LineState(OptionalDouble.empty())).layer(VIEW_OFFSET_Z_LAYERING).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_ENTITY_TARGET).depthTest(DEPTH_ALWAYS).writeMask(COLOR_DEPTH_WRITE).build(false));
	public static final RenderType ENTITY_COLOR = makeType("cpm:entity_color_translucent_cull", DefaultVertexFormats.ENTITY, 7, 256, true, true, RenderType.State.getBuilder().transparency(TRANSLUCENT_TRANSPARENCY).texture(NO_TEXTURE).diffuseLighting(DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(true));

	public CustomRenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static RenderType getLinesNoDepth() {
		return LINES_NO_NEPTH;
	}

	public static RenderType getEntityColorTranslucentCull() {
		return ENTITY_COLOR;
	}

	public static RenderType getEntityTranslucentCullNoLight(ResourceLocation locationIn) {
		RenderType.State rendertype$state = RenderType.State.getBuilder().texture(new RenderState.TextureState(locationIn, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(DIFFUSE_LIGHTING_DISABLED).alpha(DEFAULT_ALPHA).lightmap(LIGHTMAP_DISABLED).overlay(OVERLAY_DISABLED).build(true);
		return makeType("cpm:entity_translucent_cull_nolight", DefaultVertexFormats.ENTITY, 7, 256, true, true, rendertype$state);
	}

	public static RenderType getTexCutout(ResourceLocation locationIn) {
		RenderType.State rendertype$state = RenderType.State.getBuilder().texture(new RenderState.TextureState(locationIn, false, false)).transparency(NO_TRANSPARENCY).cull(CULL_DISABLED).alpha(DEFAULT_ALPHA).build(true);
		return makeType("cpm:cutout", DefaultVertexFormats.POSITION_TEX, 7, 256, true, false, rendertype$state);
	}
}
