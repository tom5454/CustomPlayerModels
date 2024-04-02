package com.tom.cpm.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.option.KeyBinding;
import net.modificationstation.stationapi.api.client.event.option.KeyBindingRegisterEvent;

import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.KeyboardEvent;

public class KeyBindings implements IKeybind {
	public static KeyBinding gestureMenuBinding, renderToggleBinding;
	public static IKeybind[] quickAccess = new IKeybind[IKeybind.QUICK_ACCESS_KEYBINDS_COUNT];

	public static void init(KeyBindingRegisterEvent evt) {
		gestureMenuBinding = new KeyBinding("key.cpm.gestureMenu", Keyboard.KEY_G);
		kbs.add(new KeyBindings(evt, gestureMenuBinding, "gestureMenu"));

		renderToggleBinding = new KeyBinding("key.cpm.renderToggle", 0);
		kbs.add(new KeyBindings(evt, renderToggleBinding, "renderToggle"));

		for(int i = 1;i<=IKeybind.QUICK_ACCESS_KEYBINDS_COUNT;i++)
			createQA(evt, i);
	}

	private static void createQA(KeyBindingRegisterEvent evt, int id) {
		KeyBinding kb = new KeyBinding("key.cpm.qa_" + id, 0);
		KeyBindings kbs = new KeyBindings(evt, kb, "qa_" + id);
		KeyBindings.kbs.add(kbs);
		quickAccess[id - 1] = kbs;
	}

	public static List<IKeybind> kbs = new ArrayList<>();
	private final KeyBinding kb;
	private final String name;

	private KeyBindings(KeyBindingRegisterEvent evt, KeyBinding kb, String name) {
		this.kb = kb;
		this.name = name;
		evt.keyBindings.add(kb);
	}

	@Override
	public boolean isPressed(KeyboardEvent evt) {
		return kb.code == evt.keyCode;
	}

	@Override
	public String getBoundKey() {
		return Keyboard.getKeyName(kb.code);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isPressed() {
		return kb.code != Keyboard.KEY_NONE && Keyboard.isKeyDown(kb.code);
	}
}
