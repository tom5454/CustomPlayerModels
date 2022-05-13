package com.tom.cpl.render;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tom.cpl.render.VBuffers.NativeRenderType;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.retro.RetroGLAccess;
import com.tom.cpm.shared.retro.RetroGLAccess.RetroLayer;
import com.tom.cpm.shared.skin.TextureProvider;

public class RenderTypeBuilder<RL, RT> {
	private Map<RenderMode, Function<RL, RT>> modeFactories = new EnumMap<>(RenderMode.class);
	private Map<RenderMode, Integer> renderLayers = new EnumMap<>(RenderMode.class);
	private Supplier<RL> getDynamic;

	public void build(RenderTypes<RenderMode> renderTypes, TextureHandler<RL, RT> handler) {
		renderTypes.put(RenderMode.NORMAL, new NativeRenderType(0));
		for(Entry<RenderMode, Function<RL, RT>> e : modeFactories.entrySet()) {
			int layer = renderLayers.get(e.getKey());
			renderTypes.put(e.getKey(), new NativeRenderType(e.getValue().apply(handler.getTexture()), layer));
		}
		renderTypes.put(RenderMode.DEFAULT, new NativeRenderType(handler.getRenderType(), 0));
	}

	public void build(RenderTypes<RenderMode> renderTypes, RL texture) {
		renderTypes.put(RenderMode.NORMAL, new NativeRenderType(0));
		for(Entry<RenderMode, Function<RL, RT>> e : modeFactories.entrySet()) {
			int layer = renderLayers.get(e.getKey());
			renderTypes.put(e.getKey(), new NativeRenderType(e.getValue().apply(texture), layer));
		}
	}

	public void build(RenderTypes<RenderMode> renderTypes, TextureProvider texture) {
		if(getDynamic == null)throw new IllegalStateException("Get dynamic is not initialized");
		RL tex = null;
		if(texture != null) {
			texture.bind();
			tex = getDynamic.get();
		}
		renderTypes.put(RenderMode.NORMAL, new NativeRenderType(modeFactories.get(RenderMode.DEFAULT).apply(tex), 0));
		for(Entry<RenderMode, Function<RL, RT>> e : modeFactories.entrySet()) {
			int layer = renderLayers.get(e.getKey());
			renderTypes.put(e.getKey(), new NativeRenderType(e.getValue().apply(tex), layer));
		}
	}

	public static interface TextureHandler<RL, RT> {
		RL getTexture();
		void setTexture(RL texture);
		RT getRenderType();
	}

	public static <RL> RenderTypeBuilder<RL, RetroLayer> setupRetro(RetroGLAccess<RL> gl) {
		RenderTypeBuilder<RL, RetroLayer> this0 = new RenderTypeBuilder<>();
		this0.register(RenderMode.DEFAULT, gl::texture, 0);
		this0.register(RenderMode.GLOW, gl::eyes, 1);
		this0.register(RenderMode.COLOR, gl::color, 0);
		this0.register(RenderMode.COLOR_GLOW, gl::color, 1);
		this0.register(RenderMode.OUTLINE, gl::linesNoDepth, 2);
		this0.getDynamic = gl::getDynTexture;
		return this0;
	}

	public RenderTypeBuilder<RL, RT> register(RenderMode mode, Function<RL, RT> factory, int layer) {
		if(mode == RenderMode.NORMAL)throw new IllegalArgumentException("Can't init built-in layer");
		modeFactories.put(mode, factory);
		renderLayers.put(mode, layer);
		return this;
	}

	public RenderTypeBuilder<RL, RT> register(RenderMode mode, Supplier<RT> factory, int layer) {
		if(mode == RenderMode.NORMAL)throw new IllegalArgumentException("Can't init built-in layer");
		modeFactories.put(mode, __ -> factory.get());
		renderLayers.put(mode, layer);
		return this;
	}
}
