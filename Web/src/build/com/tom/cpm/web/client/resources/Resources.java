package com.tom.cpm.web.client.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Resources {
	private static final Map<String, String> RESOURCE_MAP;

	static {
		RESOURCE_MAP = new HashMap<>();
		//${fill_resource_map_lqsnlna}$
	}

	private static void i(String key, String value) {
		RESOURCE_MAP.put(key, value);
	}

	public static String getResource(String path) {
		return RESOURCE_MAP.get(path);
	}

	public static Set<String> listResources() {
		return RESOURCE_MAP.keySet();
	}
}
