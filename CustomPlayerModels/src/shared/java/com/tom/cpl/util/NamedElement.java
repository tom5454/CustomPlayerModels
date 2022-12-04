package com.tom.cpl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
		private Collection<T> in;
		private List<NamedElement<T>> list;
		private Map<T, NamedElement<T>> elems;
		private Consumer<NamedElement<T>> setter;
		private Function<T, String> tostring;

		public NameMapper(Collection<T> in, Function<T, String> tostring) {
			this.in = in;
			this.tostring = tostring;
			elems = new HashMap<>();
			list = new ArrayList<>();
			for (T t : in) {
				NamedElement<T> e = new NamedElement<>(t, tostring);
				elems.put(t, e);
				list.add(e);
			}
		}

		public NameMapper(T[] in, Function<T, String> tostring) {
			this(Arrays.asList(in), tostring);
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

		public Comparator<NamedElement<T>> cmp(Comparator<T> c) {
			return (a, b) -> c.compare(a.getElem(), b.getElem());
		}

		public void refreshValues() {
			Iterator<Entry<T, NamedElement<T>>> i = elems.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry<T, NamedElement<T>> entry = i.next();
				if(!in.contains(entry.getKey()))i.remove();
			}
			for (T t : in) {
				if(!elems.containsKey(t)) {
					NamedElement<T> e = new NamedElement<>(t, tostring);
					elems.put(t, e);
					list.add(e);
				}
			}
			if(in instanceof List) {
				Map<T, Integer> id = new HashMap<>();
				int j = 0;
				for (T t : in)id.put(t, j++);
				list.sort(Comparator.comparingInt(v -> id.getOrDefault(v.getElem(), Integer.MAX_VALUE)));
			}
		}
	}
}
