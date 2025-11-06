package com.tom.cpm.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;

import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.KeyboardEvent;

public class KeyBindings implements IKeybind {
	public static KeyMapping.Category category;
	private static KeyConflictCtx conflictCtx = new KeyConflictCtx();
	public static KeyMapping gestureMenuBinding, renderToggleBinding;
	public static IKeybind[] quickAccess = new IKeybind[IKeybind.QUICK_ACCESS_KEYBINDS_COUNT];

	public static void init(RegisterKeyMappingsEvent evt) {
		category = KeyMapping.Category.register(ResourceLocation.tryBuild("cpm", "keys"));

		gestureMenuBinding = new KeyMapping("key.cpm.gestureMenu", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_H), category);
		renderToggleBinding = new KeyMapping("key.cpm.renderToggle", KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, category);
		kbs.add(new KeyBindings(gestureMenuBinding, "gestureMenu", evt));
		kbs.add(new KeyBindings(renderToggleBinding, "renderToggle", evt));

		for(int i = 1;i<=IKeybind.QUICK_ACCESS_KEYBINDS_COUNT;i++)
			createQA(i, evt);
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

	private static void createQA(int id, RegisterKeyMappingsEvent evt) {
		KeyMapping kb = new KeyMapping("key.cpm.qa_" + id, conflictCtx, InputConstants.UNKNOWN, category);
		KeyBindings kbs = new KeyBindings(kb, "qa_" + id, evt);
		KeyBindings.kbs.add(kbs);
		quickAccess[id - 1] = kbs;
	}

	public static List<IKeybind> kbs = new ArrayList<>();
	private final KeyMapping kb;
	private final String name;

	private KeyBindings(KeyMapping kb, String name, RegisterKeyMappingsEvent evt) {
		this.kb = kb;
		this.name = name;
		evt.register(kb);
	}

	@Override
	public boolean isPressed(KeyboardEvent evt) {
		InputConstants.Key mouseKey = InputConstants.getKey(new KeyEvent(evt.keyCode, evt.scancode, 0));
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
