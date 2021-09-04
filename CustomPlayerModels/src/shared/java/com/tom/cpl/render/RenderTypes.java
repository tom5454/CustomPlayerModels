package com.tom.cpl.render;

import java.util.EnumMap;

import com.tom.cpl.render.VBuffers.NativeRenderType;

public class RenderTypes<E extends Enum<E>> {
	private EnumMap<E, NativeRenderType> types;

	public RenderTypes(Class<E> clazz) {
		types = new EnumMap<>(clazz);
	}

	public NativeRenderType get(E key) {
		return types.get(key);
	}

	public NativeRenderType put(E key, NativeRenderType value) {
		return types.put(key, value);
	}

	public void clear() {
		types.clear();
	}

	public boolean isInitialized() {
		return !types.isEmpty();
	}

	public void putAll(RenderTypes<E> cbi) {
		types.putAll(cbi.types);
	}
}
