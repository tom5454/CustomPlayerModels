package com.tom.ugwt.client;

import java.util.UUID;

import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class GlobalFunc implements Runnable {
	private static final JsPropertyMap<Object> WINDOW = Js.uncheckedCast(DomGlobal.window);
	private final String id;

	private GlobalFunc(String id) {
		this.id = id;
	}

	public static <T> GlobalFunc pushGlobalFunc(Class<T> type, T func) {
		String id = "$cpmfunc_" + UUID.randomUUID().toString().replace('-', '_');
		WINDOW.set(id, func);
		return new GlobalFunc(id);
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public void run() {
		WINDOW.delete(id);
	}
}
