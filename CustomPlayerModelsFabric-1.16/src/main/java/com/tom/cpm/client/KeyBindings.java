package com.tom.cpm.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

import com.tom.cpm.shared.gui.Gui.KeyboardEvent;
import com.tom.cpm.shared.gui.IKeybind;

public class KeyBindings implements IKeybind {
	public static KeyBinding gestureMenuBinding, renderToggleBinding;
	public static Map<Integer, KeyBinding> quickAccess = new HashMap<>();
	public static void init() {
		gestureMenuBinding = new KeyBinding("key.cpm.gestureMenu", GLFW.GLFW_KEY_G, "key.cpm.category");
		kbs.add(new KeyBindings(gestureMenuBinding, "gestureMenu"));

		renderToggleBinding = new KeyBinding("key.cpm.renderToggle", InputUtil.UNKNOWN_KEY.getCode(), "key.cpm.category");
		kbs.add(new KeyBindings(renderToggleBinding, "renderToggle"));

		for(int i = 1;i<=6;i++)
			createQA(i);
	}

	private static void createQA(int id) {
		KeyBinding kb = new KeyBinding("key.cpm.qa_" + id, InputUtil.UNKNOWN_KEY.getCode(), "key.cpm.category");
		kbs.add(new KeyBindings(kb, "qa_" + id));
		quickAccess.put(id, kb);
	}

	public static List<IKeybind> kbs = new ArrayList<>();
	private final KeyBinding kb;
	private final String name;

	private KeyBindings(KeyBinding kb, String name) {
		this.kb = kb;
		this.name = name;
		KeyBindingHelper.registerKeyBinding(kb);
	}

	@Override
	public boolean isPressed(KeyboardEvent evt) {
		return kb.matchesKey(evt.keyCode, evt.scancode);
	}

	@Override
	public String getBoundKey() {
		return kb.getBoundKeyLocalizedText().getString();
	}

	@Override
	public String getName() {
		return name;
	}
}
