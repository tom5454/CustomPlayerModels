package com.tom.cpm.client;

import java.util.OptionalDouble;

import net.coderbot.batchedentityrendering.impl.TransparencyType;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

import com.tom.cpm.client.optifine.proxy.BlendingStateHolderEx;

public class CustomRenderTypes extends RenderType {
	private static final RenderType LINES_NO_DEPTH = create("cpm:lines_no_depth", DefaultVertexFormats.POSITION_COLOR, 1, 256, RenderType.State.builder().setLineState(new RenderState.LineState(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setDepthTestState(NO_DEPTH_TEST).setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false));

	private CustomRenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static RenderType entityColorTranslucent() {
		return entityTranslucent(Platform.WHITE);
	}

	public static RenderType glowingEyesColor() {
		return glowingEyes(Platform.WHITE);
	}

	public static RenderType linesNoDepth() {
		return LINES_NO_DEPTH;
	}

	public static RenderType glowingEyes(ResourceLocation rl) {
		RenderType rt = RenderType.eyes(rl);
		if (ClientBase.irisLoaded)
			((BlendingStateHolderEx) rt).setTransparencyType(TransparencyType.DECAL);
		return rt;
	}
}
