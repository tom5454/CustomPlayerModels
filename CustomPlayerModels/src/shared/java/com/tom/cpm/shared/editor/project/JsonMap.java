package com.tom.cpm.shared.editor.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface JsonMap {

	Object get(String name);
	JsonMap getMap(String name);
	JsonList getList(String name);
	void forEach(BiConsumer<String, Object> c);

	default Object getOrDefault(String name, Object def) {
		Object v = get(name);
		if(v == null)return def;
		return v;
	}

	default int getInt(String name) {
		return ((Number)get(name)).intValue();
	}

	default int getInt(String name, int def) {
		return ((Number)getOrDefault(name, def)).intValue();
	}

	default float getFloat(String name) {
		return ((Number)get(name)).floatValue();
	}

	default float getFloat(String name, float def) {
		return ((Number)getOrDefault(name, def)).floatValue();
	}

	default boolean getBoolean(String name) {
		return (boolean) get(name);
	}

	default boolean getBoolean(String name, boolean def) {
		return (boolean) getOrDefault(name, def);
	}

	default String getString(String name) {
		return (String) get(name);
	}

	default String getString(String name, String def) {
		return (String) getOrDefault(name, def);
	}

	default long getLong(String name) {
		return ((Number)get(name)).longValue();
	}

	default long getLong(String name, long def) {
		return ((Number)getOrDefault(name, def)).longValue();
	}

	default boolean containsKey(String name) {
		return get(name) != null;
	}

	Map<String, Object> asMap();

	void put(String name, Object data);

	default JsonMap putMap(String name) {
		JsonMap map = getMap(name);
		if(map == null) {
			put(name, new HashMap<>());
		}
		return getMap(name);
	}

	default JsonList putList(String name) {
		JsonList map = getList(name);
		if(map == null) {
			put(name, new ArrayList<>());
		}
		return getList(name);
	}

	default <T> T getObject(String name, BiFunction<JsonMap, T, T> ctr, T def) {
		return ctr.apply(getMap(name), def);
	}
}
