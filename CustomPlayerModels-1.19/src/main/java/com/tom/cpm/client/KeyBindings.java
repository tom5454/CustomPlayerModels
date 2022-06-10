package com.tom.cpm.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;

import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.KeyboardEvent;

public class KeyBindings implements IKeybind {
	private static KeyConflictCtx conflictCtx = new KeyConflictCtx();
	public static KeyMapping gestureMenuBinding, renderToggleBinding;
	public static Map<Integer, KeyMapping> quickAccess = new HashMap<>();
	public static void init() {
		gestureMenuBinding = new KeyMapping("key.cpm.gestureMenu", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_G), "key.cpm.category");
		renderToggleBinding = new KeyMapping("key.cpm.renderToggle", KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, "key.cpm.category");
		kbs.add(new KeyBindings(gestureMenuBinding, "gestureMenu"));
		kbs.add(new KeyBindings(renderToggleBinding, "renderToggle"));

		for(int i = 1;i<=6;i++)
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
		KeyMapping kb = new KeyMapping("key.cpm.qa_" + id, conflictCtx, InputConstants.UNKNOWN, "key.cpm.category");
		kbs.add(new KeyBindings(kb, "qa_" + id));
		quickAccess.put(id, kb);
	}

	public static List<IKeybind> kbs = new ArrayList<>();
	private final KeyMapping kb;
	private final String name;

	private KeyBindings(KeyMapping kb, String name) {
		this.kb = kb;
		this.name = name;
		ClientRegistry.registerKeyBinding(kb);
	}

	@Override
	public boolean isPressed(KeyboardEvent evt) {
		InputConstants.Key mouseKey = InputConstants.getKey(evt.keyCode, evt.scancode);
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
}
