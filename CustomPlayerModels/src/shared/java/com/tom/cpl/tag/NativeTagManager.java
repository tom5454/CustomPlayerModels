package com.tom.cpl.tag;

import java.util.Collections;
import java.util.List;

public interface NativeTagManager<T> {
	public abstract List<String> listNativeTags(T stack);
	public abstract List<T> listNativeEntries(String tag);
	public abstract List<String> listNativeTags();
	public abstract List<T> getAllElements();
	public abstract boolean isInNativeTag(String tag, T stack);
	public abstract T emptyObject();
	public abstract String getId(T type);

	public static final NativeTagManager<?> EMPTY = new NativeTagManager<Object>() {

		@Override
		public List<String> listNativeTags(Object stack) {
			return Collections.emptyList();
		}

		@Override
		public List<Object> listNativeEntries(String tag) {
			return Collections.emptyList();
		}

		@Override
		public List<String> listNativeTags() {
			return Collections.emptyList();
		}

		@Override
		public boolean isInNativeTag(String tag, Object stack) {
			return false;
		}

		@Override
		public Object emptyObject() {
			return null;
		}

		@Override
		public List<Object> getAllElements() {
			return Collections.emptyList();
		}

		@Override
		public String getId(Object type) {
			return "";
		}
	};

	@SuppressWarnings("unchecked")
	public static <T> NativeTagManager<T> empty() {
		return (NativeTagManager<T>) EMPTY;
	}
}
