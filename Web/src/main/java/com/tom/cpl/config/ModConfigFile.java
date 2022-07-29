package com.tom.cpl.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.BuiltInSafetyProfiles;
import com.tom.cpm.shared.config.ConfigKeys;

import elemental2.dom.DomGlobal;
import elemental2.dom.Window;
import elemental2.webstorage.Storage;
import elemental2.webstorage.WebStorageWindow;

public class ModConfigFile extends ConfigEntry {
	private boolean changed = false;
	private Storage local;

	@SuppressWarnings("unchecked")
	public ModConfigFile(Window window, boolean doLoad) {
		changeListener = () -> changed = true;
		data = new TreeMap<>();

		if(doLoad) {
			try {
				local = WebStorageWindow.of(window).localStorage;
				if(local.getItem("config") != null) {
					data = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(local.getItem("config"), Object.class);
				}
			} catch (Throwable e) {
				DomGlobal.console.log(e);
			}
		}
		getEntry(ConfigKeys.GLOBAL_SETTINGS).setString(ConfigKeys.SAFETY_PROFILE, BuiltInSafetyProfiles.OFF.name().toLowerCase());
	}

	public void save() {
		if(changed && local != null) {
			local.setItem("config", MinecraftObjectHolder.gson.toJson(data));
			changed = false;
		}
	}

	public ConfigEntryTemp createTemp() {
		return new ConfigEntryTemp(data);
	}

	public class ConfigEntryTemp extends ConfigEntry {
		private boolean dirty;

		public ConfigEntryTemp(Map<String, Object> map) {
			changeListener = this::markDirty;
			this.data = new HashMap<>(map);
		}

		public void saveConfig() {
			ModConfigFile.this.data = data;
			changed = true;
			ModConfigFile.this.entries.clear();
			ModConfigFile.this.lists.clear();
			save();
			dirty = false;
		}

		@Override
		public ConfigEntry getEntry(String name) {
			return entries.computeIfAbsent(name, k -> new ConfigEntry(this.<Map<String, Object>>mapGet(name, HashMap::new, HashMap::new), changeListener));
		}

		@Override
		public ConfigEntryList getEntryList(String name) {
			return lists.computeIfAbsent(name, k -> new ConfigEntryList(this.<List<Object>>mapGet(name, ArrayList::new, ArrayList::new), changeListener));
		}

		@SuppressWarnings("unchecked")
		private <T> T mapGet(String name, Supplier<T> newV, UnaryOperator<T> copy) {
			return (T) data.compute(name, (k, v) -> v == null ? newV.get() : copy.apply((T) v));
		}

		public void markDirty() {
			dirty = true;
		}

		public boolean isDirty() {
			return dirty;
		}
	}
}