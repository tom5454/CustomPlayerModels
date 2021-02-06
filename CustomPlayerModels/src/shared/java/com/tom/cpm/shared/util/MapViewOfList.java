package com.tom.cpm.shared.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MapViewOfList<L, K, V> extends AbstractMap<K, V> {
	private Set<Entry<K, V>> entrySet;
	public MapViewOfList(List<L> list, Function<L, K> key, Function<L, V> value) {
		this.entrySet = new AbstractSet<Map.Entry<K,V>>() {

			@Override
			public Iterator<Entry<K, V>> iterator() {
				Iterator<L> l = list.iterator();
				return new Iterator<Map.Entry<K,V>>() {

					@Override
					public boolean hasNext() {
						return l.hasNext();
					}

					@Override
					public Entry<K, V> next() {
						L elem = l.next();
						return new Entry<K, V>() {

							@Override
							public K getKey() {
								return key.apply(elem);
							}

							@Override
							public V getValue() {
								return value.apply(elem);
							}

							@Override
							public V setValue(V value) {
								throw new UnsupportedOperationException();
							}
						};
					}
				};
			}

			@Override
			public int size() {
				return list.size();
			}
		};
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return entrySet;
	}
}
