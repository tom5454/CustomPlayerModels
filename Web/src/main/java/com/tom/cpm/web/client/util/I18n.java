package com.tom.cpm.web.client.util;

import java.nio.charset.StandardCharsets;

import com.tom.cpm.web.client.java.Base64;
import com.tom.cpm.web.client.resources.Resources;

import elemental2.core.Global;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import elemental2.webstorage.WebStorageWindow;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class I18n {
	private static JsPropertyMap<String> entries;
	public static final Promise<Object> loaded;
	public static String locale;

	static {
		String loc = DomGlobal.navigator.language.toLowerCase().replace('-', '_');
		try {
			locale = WebStorageWindow.of(DomGlobal.window).localStorage.getItem("editorLanguage");
		} catch (Exception e) {
			locale = loc;
		}
		DomGlobal.console.log("Selected Language: " + locale);
		loaded = Resources.loaded.then(__ -> {
			entries = getLang("en_us");
			JsPropertyMap<String> ent = getLang(locale);
			if (JsObject.keys(ent).length == 0 && !loc.equals(locale)) {
				locale = loc;
				ent = getLang(locale);
			}
			final JsPropertyMap<String> e = ent;
			if (JsObject.keys(e).length > 0)
				e.forEach(k -> entries.set(k, e.get(k)));
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

	public static String format(String loc, Object... args) {
		return String.format(get(loc), args);
	}

	public static String formatNl(String loc, Object... args) {
		String f = format(loc, args);
		return f.replace("\\", "\n");
	}

	public static String formatBr(String loc, Object... args) {
		String f = format(loc, args);
		return f.replace("\\", "<br>");
	}
}
