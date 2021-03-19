package com.tom.cpm.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.KeyboardEvent;

import cpw.mods.fml.client.registry.ClientRegistry;

public class KeyBindings implements IKeybind {
	public static KeyBinding gestureMenuBinding, renderToggleBinding;
	public static Map<Integer, KeyBinding> quickAccess = new HashMap<>();

	public static void init() {
		gestureMenuBinding = new KeyBinding("key.cpm.gestureMenu", Keyboard.KEY_G, "key.cpm.category");
		kbs.add(new KeyBindings(gestureMenuBinding, "gestureMenu"));

		renderToggleBinding = new KeyBinding("key.cpm.renderToggle", 0, "key.cpm.category");
		kbs.add(new KeyBindings(renderToggleBinding, "renderToggle"));

		for(int i = 1;i<=6;i++)
			createQA(i);
	}

	private static void createQA(int id) {
		KeyBinding kb = new KeyBinding("key.cpm.qa_" + id, 0, "key.cpm.category");
		kbs.add(new KeyBindings(kb, "qa_" + id));
		quickAccess.put(id, kb);
	}

	public static List<IKeybind> kbs = new ArrayList<>();
	private final KeyBinding kb;
	private final String name;

	private KeyBindings(KeyBinding kb, String name) {
		this.kb = kb;
		this.name = name;
		ClientRegistry.registerKeyBinding(kb);
	}

	@Override
	public boolean isPressed(KeyboardEvent evt) {
		return kb.getKeyCode() == evt.keyCode;
	}

	@Override
	public String getBoundKey() {
		return GameSettings.getKeyDisplayString(kb.getKeyCode());
	}

	@Override
	public String getName() {
		return name;
	}
}
