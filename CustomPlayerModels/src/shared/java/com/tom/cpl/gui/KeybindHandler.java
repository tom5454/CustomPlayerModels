package com.tom.cpl.gui;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.tom.cpm.shared.config.Keybind;

public class KeybindHandler {
	private final IGui gui;
	private Map<Keybind, Runnable> keyHandlers = new HashMap<>();

	public KeybindHandler(IGui gui) {
		this.gui = gui;
	}

	public void registerKeybind(Keybind kb, Runnable r) {
		keyHandlers.put(kb, r);
	}

	public void keyEvent(KeyboardEvent evt) {
		if(evt.isConsumed())return;
		keyHandlers.entrySet().stream().sorted(Comparator.comparingInt(e -> -e.getKey().getMod())).filter(e -> {
			if(e.getKey().isPressed(gui, evt)) {
				e.getValue().run();
				evt.consume();
			}
			return evt.isConsumed();
		}).findFirst();
		keyHandlers.clear();
	}
}
