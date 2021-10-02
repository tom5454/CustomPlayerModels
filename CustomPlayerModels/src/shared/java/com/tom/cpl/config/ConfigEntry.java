package com.tom.cpl.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigEntry {
	protected Map<String, ConfigEntry> entries = new HashMap<>();
	protected Map<String, ConfigEntryList> lists = new HashMap<>();
	protected Map<String, Object> data;
	protected Runnable changeListener;

	private ConfigEntry() {
	}

	public ConfigEntry(Map<String, Object> data, Runnable changeListener) {
		this.data = data;
		this.changeListener = changeListener;
	}

	public static class ModConfigFile extends ConfigEntry {
		public static final Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
		private File cfgFile;
		private boolean changed = false;

		@SuppressWarnings("unchecked")
		public ModConfigFile(File file) {
			this.cfgFile = file;
			changeListener = () -> changed = true;
			if(cfgFile.exists()) {
				try(FileReader rd = new FileReader(cfgFile)) {
					data = (Map<String, Object>) gson.fromJson(rd, Object.class);
				} catch (Exception e) {
				}
			}
			if(data == null) {
				data = new HashMap<>();
				changed = true;
			}
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

	public String getString(String name, String def) {
		try {
			return (String) data.getOrDefault(name, def);
		} catch (ClassCastException e) {
			return def;
		}
	}

	public String getSetString(String name, String def) {
		try {
			return getSet(name, def);
		} catch (ClassCastException e) {
			return def;
		}
	}

	public void setString(String name, String value) {
		data.put(name, value);
		changeListener.run();
	}

	public void clearValue(String name) {
		data.remove(name);
		entries.remove(name);
		changeListener.run();
	}

	public int getInt(String name, int def) {
		try {
			return ((Number) data.getOrDefault(name, def)).intValue();
		} catch (ClassCastException e) {
			return def;
		}
	}

	public int getSetInt(String name, int def) {
		try {
			return getSet(name, def);
		} catch (ClassCastException e) {
			return def;
		}
	}

	public void setInt(String name, int value) {
		data.put(name, value);
		changeListener.run();
	}

	public boolean getBoolean(String name, boolean def) {
		try {
			return (boolean) data.getOrDefault(name, def);
		} catch (ClassCastException e) {
			return def;
		}
	}

	public boolean getSetBoolean(String name, boolean def) {
		try {
			return getSet(name, def);
		} catch (ClassCastException e) {
			return def;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getSet(String name, T def) {
		T v = (T) data.get(name);
		if(v == null) {
			v = def;
			data.put(name, def);
			changeListener.run();
		}
		return v;
	}

	public void setBoolean(String name, boolean value) {
		data.put(name, value);
		changeListener.run();
	}

	@SuppressWarnings("unchecked")
	public ConfigEntry getEntry(String name) {
		return entries.computeIfAbsent(name, k -> new ConfigEntry((Map<String, Object>) data.computeIfAbsent(name, k2 -> new HashMap<>()), changeListener));
	}

	@SuppressWarnings("unchecked")
	public ConfigEntryList getEntryList(String name) {
		return lists.computeIfAbsent(name, k -> new ConfigEntryList((List<Object>) data.computeIfAbsent(name, k2 -> new ArrayList<>()), changeListener));
	}

	public boolean hasEntry(String name) {
		return data.containsKey(name);
	}

	public Set<String> keySet() {
		return data.keySet();
	}

	public static class ConfigEntryList {
		protected List<Object> data;
		protected Runnable changeListener;

		public ConfigEntryList(List<Object> data, Runnable changeListener) {
			this.data = data;
			this.changeListener = changeListener;
		}

		public int size() {
			return data.size();
		}

		public boolean add(Object e) {
			return data.add(e);
		}

		public void clear() {
			data.clear();
		}

		public Object get(int index) {
			return data.get(index);
		}

		public boolean contains(Object o) {
			return data.contains(o);
		}
	}
}
