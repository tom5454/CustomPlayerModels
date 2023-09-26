package com.tom.cpl.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigEntry {
	protected Map<String, ConfigEntry> entries = new HashMap<>();
	protected Map<String, ConfigEntryList> lists = new HashMap<>();
	protected Map<String, Object> data;
	protected Runnable changeListener;

	protected ConfigEntry() {
	}

	public ConfigEntry(Map<String, Object> data, Runnable changeListener) {
		this.data = data;
		this.changeListener = changeListener;
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
		lists.remove(name);
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
			return getSet(name, (Number) Integer.valueOf(def)).intValue();
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

	public float getFloat(String name, float def) {
		try {
			return ((Number) data.getOrDefault(name, def)).floatValue();
		} catch (ClassCastException e) {
			return def;
		}
	}

	public float getSetFloat(String name, float def) {
		try {
			return getSet(name, (Number) Float.valueOf(def)).floatValue();
		} catch (ClassCastException e) {
			return def;
		}
	}

	public void setFloat(String name, float value) {
		data.put(name, value);
		changeListener.run();
	}

	public void clear() {
		data.clear();
		entries.clear();
		lists.clear();
		changeListener.run();
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
			boolean s = data.add(e);
			if(s)changeListener.run();
			return s;
		}

		public void clear() {
			data.clear();
			changeListener.run();
		}

		public Object get(int index) {
			return data.get(index);
		}

		public boolean contains(Object o) {
			return data.contains(o);
		}
	}
}
