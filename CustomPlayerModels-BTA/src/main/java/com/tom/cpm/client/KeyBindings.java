package com.tom.cpm.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.options.components.KeyBindingComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.KeyBinding;

import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.KeyboardEvent;

public class KeyBindings implements IKeybind {
	public static KeyBinding gestureMenuBinding, renderToggleBinding;
	public static IKeybind[] quickAccess = new IKeybind[IKeybind.QUICK_ACCESS_KEYBINDS_COUNT];
	private static OptionsCategory cpmBinds;

	public static void init() {
		cpmBinds = new OptionsCategory("key.cpm.category");

		gestureMenuBinding = new KeyBinding("key.cpm.gestureMenu").setDefault(InputDevice.keyboard, Keyboard.KEY_G);
		kbs.add(new KeyBindings(gestureMenuBinding, "gestureMenu"));

		renderToggleBinding = new KeyBinding("key.cpm.renderToggle");
		kbs.add(new KeyBindings(renderToggleBinding, "renderToggle"));

		for(int i = 1;i<=IKeybind.QUICK_ACCESS_KEYBINDS_COUNT;i++)
			createQA(i);

		kbs.forEach(e -> GameSettings.keys.add(((KeyBindings) e).kb));

		OptionsPages.CONTROLS.withComponent(cpmBinds);
	}

	private static void createQA(int id) {
		KeyBinding kb = new KeyBinding("key.cpm.qa_" + id);
		KeyBindings kbs = new KeyBindings(kb, "qa_" + id);
		KeyBindings.kbs.add(kbs);
		quickAccess[id - 1] = kbs;
	}

	public static List<IKeybind> kbs = new ArrayList<>();
	private final KeyBinding kb;
	private final String name;

	private KeyBindings(KeyBinding kb, String name) {
		this.kb = kb;
		this.name = name;

		cpmBinds.withComponent(new KeyBindingComponent(kb));
	}

	@Override
	public boolean isPressed(KeyboardEvent evt) {
		return kb.getKeyCode() == evt.keyCode;
	}

	@Override
	public String getBoundKey() {
		return kb.getKeyName();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isPressed() {
		return kb.isPressed();
	}
}
