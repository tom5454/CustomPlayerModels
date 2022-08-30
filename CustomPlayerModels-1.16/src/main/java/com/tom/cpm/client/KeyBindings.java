package com.tom.cpm.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;

import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.KeyboardEvent;

public class KeyBindings implements IKeybind {
	private static KeyConflictCtx conflictCtx = new KeyConflictCtx();
	public static KeyBinding gestureMenuBinding, renderToggleBinding;
	public static IKeybind[] quickAccess = new IKeybind[IKeybind.QUICK_ACCESS_KEYBINDS_COUNT];

	public static void init() {
		gestureMenuBinding = new KeyBinding("key.cpm.gestureMenu", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_G), "key.cpm.category");
		renderToggleBinding = new KeyBinding("key.cpm.renderToggle", KeyConflictContext.IN_GAME, InputMappings.UNKNOWN, "key.cpm.category");
		kbs.add(new KeyBindings(gestureMenuBinding, "gestureMenu"));
		kbs.add(new KeyBindings(renderToggleBinding, "renderToggle"));

		for(int i = 1;i<=IKeybind.QUICK_ACCESS_KEYBINDS_COUNT;i++)
			createQA(i);
	}

	private static class KeyConflictCtx implements IKeyConflictContext {

		@Override
		public boolean isActive() {
			Minecraft mc = Minecraft.getInstance();
			return mc.screen instanceof GuiImpl || (!KeyConflictContext.GUI.isActive() && mc.player != null);
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return other == this || other == KeyConflictContext.IN_GAME;
		}
	}

	private static void createQA(int id) {
		KeyBinding kb = new KeyBinding("key.cpm.qa_" + id, conflictCtx, InputMappings.UNKNOWN, "key.cpm.category");
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
		ClientRegistry.registerKeyBinding(kb);
	}

	@Override
	public boolean isPressed(KeyboardEvent evt) {
		InputMappings.Input mouseKey = InputMappings.getKey(evt.keyCode, evt.scancode);
		return kb.isActiveAndMatches(mouseKey);
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
