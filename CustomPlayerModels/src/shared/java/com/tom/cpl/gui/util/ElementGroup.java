package com.tom.cpl.gui.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ElementGroup<S, P> implements Consumer<S> {
	private Map<S, Set<P>> map = new HashMap<>();
	private BiConsumer<P, Boolean> setValue;

	public ElementGroup(BiConsumer<P, Boolean> setValue) {
		this.setValue = setValue;
	}

	public void addElement(S type, P elem) {
		map.computeIfAbsent(type, k -> new HashSet<>()).add(elem);
	}

	@Override
	public void accept(S type) {
		map.values().stream().flatMap(Set::stream).forEach(e -> setValue.accept(e, false));
		map.getOrDefault(type, Collections.emptySet()).forEach(e -> setValue.accept(e, true));
	}

	public void accept(Collection<S> type) {
		map.values().stream().flatMap(Set::stream).forEach(e -> setValue.accept(e, false));
		type.stream().map(map::get).filter(e -> e != null).flatMap(Set::stream).forEach(e -> setValue.accept(e, true));
	}

	public boolean containsKey(S key) {
		return map.containsKey(key);
	}
}
