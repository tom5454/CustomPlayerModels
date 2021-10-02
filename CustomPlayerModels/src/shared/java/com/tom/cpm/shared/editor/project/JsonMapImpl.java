package com.tom.cpm.shared.editor.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class JsonMapImpl implements JsonMap {
	private Map<String, Object> map;

	public JsonMapImpl(Map<String, Object> map) {
		this.map = map;
	}

	public JsonMapImpl() {
		map = new HashMap<>();
	}

	@Override
	public Object get(String name) {
		return map.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonMap getMap(String name) {
		Object v = get(name);
		if(v == null)return null;
		return new JsonMapImpl((Map<String, Object>) v);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonList getList(String name) {
		Object v = get(name);
		if(v == null)return null;
		return new JsonListImpl((List<Object>) v);
	}

	@Override
	public void forEach(BiConsumer<String, Object> c) {
		map.forEach(c);
	}

	@Override
	public Map<String, Object> asMap() {
		return map;
	}

	@Override
	public void put(String name, Object data) {
		map.put(name, data);
	}

}
