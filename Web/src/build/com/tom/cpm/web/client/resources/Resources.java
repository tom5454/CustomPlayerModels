package com.tom.cpm.web.client.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import elemental2.promise.Promise;

public class Resources {
	private static final Map<String, String> RESOURCE_MAP;
	public static final Promise<Void> loaded;

	static {
		RESOURCE_MAP = new HashMap<>();
		loaded = Promise.resolve((Void) null);
		//${fill_resource_map_lqsnlna}$
	}

	private static void i(String key, String value) {
		RESOURCE_MAP.put(key, value);
	}

	/* TODO compress data
	 * static {
		RESOURCE_MAP = new HashMap<>();
		loaded = DomGlobal.fetch("data:application/octet-binary;base64,${fill_resource_map_lqsnlna}$").
				then(b -> b.arrayBuffer()).then(b -> new JSZip().loadAsync(b)).then(zip -> {
					List<Promise<Object>> resolved = new ArrayList<>();
					for(String f : JsObject.keys(zip.files).asList()) {
						ZipEntry e = zip.file(f);
						if(e != null) {
							resolved.add(e.async("base64").then(dt -> {
								RESOURCE_MAP.put(f, dt);
								return null;
							}));
						}
					}
					return null;
				});
	}*/

	public static String getResource(String path) {
		return RESOURCE_MAP.get(path);
	}

	public static Set<String> listResources() {
		return RESOURCE_MAP.keySet();
	}
}
