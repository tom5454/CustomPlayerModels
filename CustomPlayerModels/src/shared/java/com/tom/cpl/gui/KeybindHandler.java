package com.tom.cpl.gui;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.tom.cpm.shared.config.Keybind;

public class KeybindHandler {
	private final IGui gui;
	private Map<Keybind, KeyHandler> keyHandlers = new HashMap<>();

	public KeybindHandler(IGui gui) {
		this.gui = gui;
	}

	public void registerKeybind(Keybind kb, Runnable r) {
		keyHandlers.put(kb, new KeyHandler(r, false));
	}

	public void registerKeybindInPopups(Keybind kb, Runnable r) {
		keyHandlers.put(kb, new KeyHandler(r, true));
	}

	public void keyEvent(KeyboardEvent evt, boolean popup) {
		if(evt.isConsumed())return;
		Stream<Entry<Keybind, KeyHandler>> s = keyHandlers.entrySet().stream();
		if (popup)s = s.filter(e -> e.getValue().inPopups);
		s.sorted(Comparator.comparingInt(e -> -e.getKey().getMod())).filter(e -> {
			if(e.getKey().isPressed(gui, evt)) {
				e.getValue().handler.run();
				evt.consume();
			}
			return evt.isConsumed();
		}).findFirst();
		keyHandlers.clear();
	}

	private static class KeyHandler {
		private Runnable handler;
		private boolean inPopups;

		public KeyHandler(Runnable handler, boolean inPopups) {
			this.handler = handler;
			this.inPopups = inPopups;
		}
	}
}
