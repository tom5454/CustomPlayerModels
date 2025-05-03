package com.tom.cpl.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tom.cpm.shared.MinecraftCommonAccess;

public class ModConfigFile extends ConfigEntry {
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
	private File cfgFile;
	private boolean changed = false;

	public ModConfigFile(File file) {
		this(file, null);
	}

	@SuppressWarnings("unchecked")
	public ModConfigFile(File file, File def) {
		this.cfgFile = file;
		changeListener = () -> changed = true;
		if(cfgFile.exists()) {
			try(FileReader rd = new FileReader(cfgFile)) {
				data = (Map<String, Object>) gson.fromJson(rd, Object.class);
			} catch (Exception e) {
			}
		} else if(def != null && def.exists()) {
			try(FileReader rd = new FileReader(def)) {
				data = (Map<String, Object>) gson.fromJson(rd, Object.class);
			} catch (Exception e) {
			}
		}
		if(data == null) {
			data = new TreeMap<>();
			changed = true;
		}
		if(def != null && !data.containsKey("__COMMENT1")) {
			data.put("__COMMENT1", "To apply this configuration to all newly generated worlds");
			data.put("__COMMENT2", "please place this configuration file into your config directory");
			data.put("__COMMENT3", "with the following name: " + def.getName());
			changed = true;
			save();
		}
		loadConfig();
	}

	public static ModConfigFile createServer(File file) {
		File cfgDir = MinecraftCommonAccess.get().getConfig().cfgFile.getParentFile();
		return new ModConfigFile(file, new File(cfgDir, file.getName().substring(0, file.getName().length() - 5) + "-server-default.json"));
	}

	public void save() {
		if(changed) {
			try(FileWriter wr = new FileWriter(cfgFile)) {
				gson.toJson(data, Object.class, wr);
				changed = false;
			} catch (Exception e) {
			}
		}
	}

	public ConfigEntryTemp createTemp() {
		return new ConfigEntryTemp(data);
	}

	public class ConfigEntryTemp extends ConfigEntry {
		private ConfigEntry ent;
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