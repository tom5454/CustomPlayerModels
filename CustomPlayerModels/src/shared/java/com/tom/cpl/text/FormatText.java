package com.tom.cpl.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpl.gui.IGui;

public class FormatText implements IText {
	private String key;
	private Object[] args;

	public FormatText(String text, Object... args) {
		this.key = text;
		this.args = args;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> m = new HashMap<>();
		m.put("key", key);
		List<Object> a = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			Object o = args[i];
			if(o instanceof IText) {
				a.add(((IText)o).toMap());
			} else {
				a.add(String.valueOf(o));
			}
		}
		m.put("args", a);
		return m;
	}

	@Override
	public String toString(IGui gui) {
		Object[] a = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			Object o = args[i];
			if(o instanceof IText) {
				a[i] = ((IText)o).toString(gui);
			} else {
				a[i] = String.valueOf(o);
			}
		}
		return gui.i18nFormat(key, a);
	}

	@Override
	public <C> C remap(TextRemapper<C> m) {
		Object[] a = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			Object o = args[i];
			if(o instanceof IText) {
				a[i] = ((IText)o).remap(m);
			} else {
				a[i] = String.valueOf(o);
			}
		}
		return m.translate(key, a);
	}

	@Override
	public String toString() {
		return key;
	}
}