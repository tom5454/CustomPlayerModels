package com.tom.cpm.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.platform.InputConstants;

import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.KeyboardEvent;

public class KeyBindings implements IKeybind {
	public static KeyMapping.Category category;
	public static KeyMapping gestureMenuBinding, renderToggleBinding;
	public static IKeybind[] quickAccess = new IKeybind[IKeybind.QUICK_ACCESS_KEYBINDS_COUNT];

	public static void init() {
		category = KeyMapping.Category.register(ResourceLocation.tryBuild("cpm", "keys"));

		gestureMenuBinding = new KeyMapping("key.cpm.gestureMenu", GLFW.GLFW_KEY_H, category);
		kbs.add(new KeyBindings(gestureMenuBinding, "gestureMenu"));

		renderToggleBinding = new KeyMapping("key.cpm.renderToggle", InputConstants.UNKNOWN.getValue(), category);
		kbs.add(new KeyBindings(renderToggleBinding, "renderToggle"));

		for(int i = 1;i<=IKeybind.QUICK_ACCESS_KEYBINDS_COUNT;i++)
			createQA(i);
	}

	private static void createQA(int id) {
		KeyMapping kb = new KeyMapping("key.cpm.qa_" + id, InputConstants.UNKNOWN.getValue(), category);
		KeyBindings kbs = new KeyBindings(kb, "qa_" + id);
		KeyBindings.kbs.add(kbs);
		quickAccess[id - 1] = kbs;
	}

	public static List<IKeybind> kbs = new ArrayList<>();
	private final KeyMapping kb;
	private final String name;

	private KeyBindings(KeyMapping kb, String name) {
		this.kb = kb;
		this.name = name;
		KeyBindingHelper.registerKeyBinding(kb);
	}

	@Override
	public boolean isPressed(KeyboardEvent evt) {
		return kb.matches(new KeyEvent(evt.keyCode, evt.scancode, 0));
	}

	@Override
	public String getBoundKey() {
		return kb.getTranslatedKeyMessage().getString();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isPressed() {
		return kb.isDown();
	}
}
