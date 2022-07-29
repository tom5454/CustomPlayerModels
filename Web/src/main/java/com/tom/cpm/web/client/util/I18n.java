package com.tom.cpm.web.client.util;

import com.tom.cpm.web.client.resources.Resources;

import elemental2.core.Global;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class I18n {
	private static JsPropertyMap<String> entries;
	private static JsPropertyMap<String> entriesFallback;

	static {
		String loc = DomGlobal.navigator.language.toLowerCase().replace('-', '_');
		String lang = Resources.getResource("assets/cpm/lang/" + loc + ".json");
		if(lang != null)
			entries = Js.uncheckedCast(Global.JSON.parse(DomGlobal.atob(lang)));
		entriesFallback = Js.uncheckedCast(Global.JSON.parse(DomGlobal.atob(Resources.getResource("assets/cpm/lang/en_us.json"))));
	}

	public static String get(String key) {
		return entries != null && entries.has(key) ? entries.get(key) : (entriesFallback.has(key) ? entriesFallback.get(key) : key);
	}
}
