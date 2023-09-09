package com.tom.cpl.util;

import java.lang.ref.WeakReference;
import java.util.function.Function;

public class WeakStorage<T> {
	private WeakReference<T> ref;

	public void set(T value) {
		if(ref == null || ref.get() != value)
			ref = new WeakReference<>(value);
	}

	public T get() {
		return ref != null ? ref.get() : null;
	}

	public <R> R call(Function<T, R> func, R def) {
		if(ref == null)return def;
		T v = ref.get();
		if(v == null)return def;
		return func.apply(v);
	}

	@SuppressWarnings("unchecked")
	public <N, R> R castCall(Class<N> clz, Function<N, R> func, R def) {
		if(ref == null)return def;
		T v = ref.get();
		if(v == null || !clz.isInstance(v))return def;
		return func.apply((N) v);
	}
}
