package com.tom.cpm.web.client.java;

import elemental2.core.JsMap;
import elemental2.core.JsObject;
import jsinterop.base.Js;

public class JsBuilder<V> {
	private JsMap<String, V> map = new JsMap<>();

	public JsBuilder<V> put(String key, V value) {
		map.set(key, value);
		return this;
	}

	public JsObject build() {
		return JsObject.fromEntries(Js.uncheckedCast(map.entries()));
	}

	public boolean isEmpty() {
		return map.size == 0;
	}

	public String first() {
		return map.keys().next().getValue();
	}
}
