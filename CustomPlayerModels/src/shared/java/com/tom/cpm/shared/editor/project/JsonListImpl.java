package com.tom.cpm.shared.editor.project;

import java.util.List;
import java.util.Map;

public class JsonListImpl implements JsonList {
	private List<Object> list;

	public JsonListImpl(List<Object> list) {
		this.list = list;
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public Object get(int i) {
		return list.get(i);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonMap getMap(int i) {
		return new JsonMapImpl((Map<String, Object>) get(i));
	}

}
