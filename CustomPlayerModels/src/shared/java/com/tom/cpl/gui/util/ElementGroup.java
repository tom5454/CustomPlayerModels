package com.tom.cpl.gui.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.tom.cpl.gui.elements.GuiElement;

public class ElementGroup<S> {
	private Map<S, Set<GuiElement>> map = new HashMap<>();

	public void addElement(S type, GuiElement elem) {
		map.computeIfAbsent(type, k -> new HashSet<>()).add(elem);
	}

	public void set(S type) {
		for (Entry<S, Set<GuiElement>> e : map.entrySet()) {
			for (GuiElement elem : e.getValue()) {
				elem.setVisible(e.getKey().equals(type));
			}
		}
	}
}
