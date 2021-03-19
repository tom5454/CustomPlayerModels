package com.tom.cpl.gui;

import java.util.HashMap;
import java.util.Map;

public class NativeGuiComponents {
	private Map<Class<?>, NativeConstructor<?, ?>> factories = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <G, N> N getNative(Class<G> clazz, G elem) {
		return ((NativeConstructor<G, N>) factories.get(clazz)).create(elem);
	}

	public <G, N> void register(Class<G> clazz, NativeConstructor<G, N> factory) {
		factories.put(clazz, factory);
	}

	@FunctionalInterface
	public static interface NativeConstructor<G, N> {
		N create(G elem);
	}
}
