package com.tom.cpm.shared.config;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyCodes;
import com.tom.cpl.gui.KeyboardEvent;

public class Keybind {
	public static final int CTRL = 2;
	public static final int SHIFT = 1;
	public static final int ALT = 4;

	private final String key;
	private final String defKey;
	private final int defMod;
	private final Key defKeyId;

	public Keybind(String key) {
		this(key, c -> 0, 0);
	}

	public Keybind(String key, Key defKeyId) {
		this(key, defKeyId, 0);
	}

	public Keybind(String key, String defKey) {
		this(key, defKey, 0);
	}

	public Keybind(String key, Key defKeyId, int defMod) {
		this.key = key;
		this.defKeyId = defKeyId;
		this.defKey = "";
		this.defMod = defMod;
	}

	public Keybind(String key, String defKey, int defMod) {
		this.key = key;
		this.defKey = defKey;
		this.defKeyId = c -> -1;
		this.defMod = defMod;
	}

	public boolean isPressed(IGui gui, KeyboardEvent evt) {
		if(evt.isConsumed())return false;
		ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS);
		if(isPressed0(gui, ce, evt)) {
			int modKeys = ce.getInt(key + ".mod", defMod);
			if(modKeys < 1)
				return true;
			if((modKeys & SHIFT) != 0 && !gui.isShiftDown())
				return false;
			if((modKeys & CTRL) != 0 && !gui.isCtrlDown())
				return false;
			if((modKeys & ALT) != 0 && !gui.isAltDown())
				return false;
			return true;
		}
		return false;
	}

	private boolean isPressed0(IGui gui, ConfigEntry ce, KeyboardEvent evt) {
		int id = ce.getInt(key, -1);
		if(id == -1) {
			String k = ce.getString(key, "");
			int def = defKeyId.get(gui.getKeyCodes());
			if(k.isEmpty())return def != -1 ? evt.matches(def) : evt.matches(defKey);
			else return evt.matches(k);
		}
		return evt.matches(id);
	}

	public int getMod() {
		return ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS).getInt(key + ".mod", defMod);
	}

	public void setKey(ConfigEntry config, KeyboardEvent evt, int mod) {
		ConfigEntry ce = config.getEntry(ConfigKeys.KEYBINDS);
		if(evt.keyName != null && !evt.keyName.isEmpty())
			ce.setString(key, evt.keyName);
		else
			ce.setInt(key, evt.keyCode);
		if(mod > 0)ce.setInt(key + ".mod", mod);
		else ce.clearValue(key + ".mod");
	}

	public void unbindKey(ConfigEntry config) {
		ConfigEntry ce = config.getEntry(ConfigKeys.KEYBINDS);
		ce.setInt(key, 0);
		ce.clearValue(key + ".mod");
	}

	public void resetKey(ConfigEntry config) {
		ConfigEntry ce = config.getEntry(ConfigKeys.KEYBINDS);
		ce.clearValue(key);
		ce.clearValue(key + ".mod");
	}

	public String getKeyName() {
		return "label.cpm.keybind." + key;
	}

	public String getSetKey(IGui gui) {
		return getSetKey(ModConfig.getCommonConfig(), gui);
	}

	public String getSetKey(ConfigEntry ce, IGui gui) {
		ce = ce.getEntry(ConfigKeys.KEYBINDS);
		String key = getSetKey0(ce, gui);
		if(key == null)return gui.i18nFormat("button.cpm.keybindNone");
		int modKeys = ce.getInt(this.key + ".mod", defMod);
		if(modKeys < 1)
			return key;
		if((modKeys & ALT) != 0)key = gui.i18nFormat("label.cpm.keybind.mod.alt", key);
		if((modKeys & SHIFT) != 0)key = gui.i18nFormat("label.cpm.keybind.mod.shift", key);
		if((modKeys & CTRL) != 0)key = gui.i18nFormat("label.cpm.keybind.mod.ctrl", key);
		return key;
	}

	private String getSetKey0(ConfigEntry ce, IGui gui) {
		int id = ce.getInt(key, -1);
		if(id == -1) {
			String k = ce.getString(key, "");
			int def = defKeyId.get(gui.getKeyCodes());
			if(k.isEmpty())return def != -1 ? (def == 0 ? null : gui.getKeyCodes().keyToString(gui, def)) : defKey;
			else return k;
		}
		if(id == 0)return null;
		return gui.getKeyCodes().keyToString(gui, id);
	}

	@FunctionalInterface
	public static interface Key {
		int get(KeyCodes c);
	}
}
