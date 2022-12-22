package com.tom.cpm.web.client.util;

import java.nio.charset.StandardCharsets;

import com.tom.cpm.web.client.java.Base64;
import com.tom.cpm.web.client.resources.Resources;

import elemental2.core.Global;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class I18n {
	private static JsPropertyMap<String> entries;
	public static final Promise<Object> loaded;

	static {
		loaded = Resources.loaded.then(__ -> {
			String loc = DomGlobal.navigator.language.toLowerCase().replace('-', '_');
			entries = getLang("en_us");
			JsPropertyMap<String> ent = getLang(loc);
			ent.forEach(k -> entries.set(k, ent.get(k)));
			return null;
		});
	}

	public static String get(String key) {
		return entries.has(key) ? entries.get(key) : key;
	}

	private static JsPropertyMap<String> getLang(String loc) {
		return Resources.listResources().stream().filter(r -> r.startsWith("assets/") && r.endsWith("/lang/" + loc + ".json")).
				map(r -> Js.<JsPropertyMap<String>>uncheckedCast(Global.JSON.parse(new String(Base64.getDecoder().decode(Resources.getResource(r)), StandardCharsets.UTF_8)))).
				reduce(Js.uncheckedCast(JsPropertyMap.of()), (a, b) -> {
					b.forEach(k -> a.set(k, b.get(k)));
					return a;
				});
	}
}
