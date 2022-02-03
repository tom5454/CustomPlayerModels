package com.tom.cpl.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class NamedElement<T> {
	private T elem;
	private Function<T, String> tostring;
	public NamedElement(T elem, Function<T, String> tostring) {
		this.elem = elem;
		this.tostring = tostring;
	}

	public T getElem() {
		return elem;
	}

	@Override
	public String toString() {
		return tostring.apply(elem);
	}

	public static class NameMapper<T> {
		private List<NamedElement<T>> list;
		private Map<T, NamedElement<T>> elems;
		private Consumer<NamedElement<T>> setter;

		public NameMapper(Iterable<T> in, Function<T, String> tostring) {
			elems = new HashMap<>();
			list = new ArrayList<>();
			for (T t : in) {
				NamedElement<T> e = new NamedElement<>(t, tostring);
				elems.put(t, e);
				list.add(e);
			}
		}

		public NameMapper(T[] in, Function<T, String> tostring) {
			elems = new HashMap<>();
			list = new ArrayList<>();
			for (T t : in) {
				NamedElement<T> e = new NamedElement<>(t, tostring);
				elems.put(t, e);
				list.add(e);
			}
		}

		public List<NamedElement<T>> asList() {
			return list;
		}

		public NamedElement<T> get(T v) {
			return elems.get(v);
		}

		public void setSetter(Consumer<NamedElement<T>> setter) {
			this.setter = setter;
		}

		public void setValue(T t) {
			setter.accept(get(t));
		}

		public void sort(Comparator<? super NamedElement<T>> c) {
			list.sort(c);
		}
	}
}
