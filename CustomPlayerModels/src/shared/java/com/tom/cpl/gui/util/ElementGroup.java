package com.tom.cpl.gui.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import com.tom.cpl.gui.elements.GuiElement;

public class ElementGroup<S> implements Consumer<S> {
	private Map<S, Set<GuiElement>> map = new HashMap<>();

	public void addElement(S type, GuiElement elem) {
		map.computeIfAbsent(type, k -> new HashSet<>()).add(elem);
	}

	@Override
	public void accept(S type) {
		for (Entry<S, Set<GuiElement>> e : map.entrySet()) {
			for (GuiElement elem : e.getValue()) {
				elem.setVisible(false);
			}
		}
		map.getOrDefault(type, Collections.emptySet()).forEach(e -> e.setVisible(true));
	}
}
