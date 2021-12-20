package com.tom.cpl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpl.gui.IGui;

public class FormatText {
	private String key;
	private Object[] args;

	public FormatText(String text, Object... args) {
		this.key = text;
		this.args = args;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> m = new HashMap<>();
		m.put("key", key);
		List<Object> a = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			Object o = args[i];
			if(o instanceof FormatText) {
				a.add(((FormatText)o).toMap());
			} else {
				a.add(String.valueOf(o));
			}
		}
		m.put("args", a);
		return m;
	}

	public String toString(IGui gui) {
		Object[] a = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			Object o = args[i];
			if(o instanceof FormatText) {
				a[i] = ((FormatText)o).toString(gui);
			} else {
				a[i] = String.valueOf(o);
			}
		}
		return gui.i18nFormat(key, a);
	}
}