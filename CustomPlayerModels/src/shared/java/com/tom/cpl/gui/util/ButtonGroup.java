package com.tom.cpl.gui.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.tom.cpl.gui.elements.GuiElement;

public class ButtonGroup<S, B extends GuiElement> implements Consumer<S> {
	private Map<S, Set<B>> map = new HashMap<>();
	private final BiConsumer<B, Boolean> setSel;
	private final BiConsumer<B, Runnable> setEvent;
	private final Consumer<S> setValue;

	public ButtonGroup(BiConsumer<B, Boolean> setSel, BiConsumer<B, Runnable> setEvent, Consumer<S> setValue) {
		this.setSel = setSel;
		this.setEvent = setEvent;
		this.setValue = setValue;
	}

	public void addElement(S type, B elem) {
		map.computeIfAbsent(type, k -> new HashSet<>()).add(elem);
		setEvent.accept(elem, () -> setValue(type));
	}

	private void setValue(S type) {
		setValue.accept(type);
		accept(type);
	}

	public void addElement(S type, Function<Runnable, B> elemF) {
		B elem = elemF.apply(() -> setValue(type));
		map.computeIfAbsent(type, k -> new HashSet<>()).add(elem);
	}

	@Override
	public void accept(S type) {
		for (Entry<S, Set<B>> e : map.entrySet()) {
			for (B elem : e.getValue()) {
				setSel.accept(elem, false);
			}
		}
		map.getOrDefault(type, Collections.emptySet()).forEach(e -> setSel.accept(e, true));
	}
}
