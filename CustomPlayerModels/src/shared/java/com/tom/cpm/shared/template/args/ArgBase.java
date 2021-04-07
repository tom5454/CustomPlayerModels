package com.tom.cpm.shared.template.args;

import java.util.List;
import java.util.Map;

import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.template.Template.IArg;

public abstract class ArgBase implements IArg {
	private String name;

	public ArgBase() {
	}

	public ArgBase(String name) {
		this.name = name;
	}

	@Override
	public void write(Map<String, Object> map) {
		map.put("value", toJson());
	}

	@Override
	public void load(Map<String, Object> map) {
		fromJson(map.get("value"));
	}

	@Override
	public void init(Map<String, Object> map) {
		name = (String) map.get("name");
		fromJson(map.get("default"));
	}

	public abstract Object toJson();
	public abstract void fromJson(Object v);

	@Override
	public void apply(Map<String, Object> map) {
		walk(map);
	}

	@SuppressWarnings("unchecked")
	private void walk(Object v) {
		String key = wrapName(name);
		if(v instanceof List) {
			((List<Object>) v).replaceAll(e -> {
				if(key.equals(v))return toJson();
				walk(e);
				return e;
			});
		} else if(v instanceof Map) {
			((Map<String, Object>) v).replaceAll((k, e) -> {
				if(key.equals(e))return toJson();
				walk(e);
				return e;
			});
		}
	}

	@Override
	public void apply(RenderedCube cube) {}

	@Override
	public void export(Map<String, Object> map) {
		map.put("default", toJson());
		map.put("name", name);
	}

	public static String wrapName(String name) {
		return "${" + name + "}$";
	}

	public String getName() {
		return name;
	}
}
