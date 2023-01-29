package com.tom.cpm.web.client.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.tom.cpm.web.client.util.JSZip;
import com.tom.cpm.web.client.util.JSZip.ZipEntry;

import elemental2.core.JsArray;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import jsinterop.base.Js;

@SuppressWarnings("unchecked")
public class Resources {
	private static final Map<String, String> RESOURCE_MAP;
	public static final Promise<Void> loaded;

	static {
		RESOURCE_MAP = new HashMap<>();
		loaded = DomGlobal.fetch("data:application/octet-binary;base64,//${fill_resource_map_lqsnlna}$").
				then(b -> b.arrayBuffer()).then(b -> new JSZip().loadAsync(b)).then(zip -> {
					DomGlobal.console.log("[CPM] Loading resources...");
					JsArray<Promise<Object>> promises = new JsArray<>();
					for(String f : JsObject.keys(zip.files).asList()) {
						ZipEntry e = zip.file(f);
						if(e != null) {
							promises.push(e.async("base64").then(dt -> {
								RESOURCE_MAP.put(f, dt);
								return null;
							}));
						}
					}
					return Promise.all(Js.cast(promises)).then(v -> {
						DomGlobal.console.log("[CPM] Loaded resources");
						return null;
					});
				});
	}

	public static String getResource(String path) {
		return RESOURCE_MAP.get(path);
	}

	public static Set<String> listResources() {
		return RESOURCE_MAP.keySet();
	}
}
