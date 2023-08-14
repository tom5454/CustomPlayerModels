package com.tom.cpl.tag;

import java.util.List;

public class NativeTag<T> implements Tag<T> {
	private final NativeTagManager<T> nativeManager;
	private final String key;

	public NativeTag(NativeTagManager<T> nativeManager, String key) {
		this.nativeManager = nativeManager;
		this.key = key;
	}

	@Override
	public boolean is(T stack) {
		return nativeManager.isInNativeTag(key, stack);
	}

	@Override
	public List<T> getAllStacks() {
		return nativeManager.listNativeEntries(key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		NativeTag other = (NativeTag) obj;
		if (key == null) {
			if (other.key != null) return false;
		} else if (!key.equals(other.key)) return false;
		return true;
	}

	@Override
	public String getId() {
		return key;
	}
}
