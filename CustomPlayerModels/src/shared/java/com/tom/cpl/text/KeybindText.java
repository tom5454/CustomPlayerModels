package com.tom.cpl.text;

import java.util.HashMap;
import java.util.Map;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpm.shared.MinecraftClientAccess;

public class KeybindText implements IText {
	private String name, fallback;

	public KeybindText(String name, String fallback) {
		this.name = name;
		this.fallback = fallback;
	}

	@Override
	public <C> C remap(TextRemapper<C> remapper) {
		if(remapper.hasKeybind()) {
			return remapper.keyBind(name);
		}
		if(MinecraftClientAccess.get() != null) {
			IKeybind rtkb = null;
			for(IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
				if(kb.getName().startsWith(fallback)) {
					rtkb = kb;
				}
			}

			String k = rtkb == null ? "?" : rtkb.getBoundKey();
			if(k.isEmpty())return remapper.translate("label.cpm.key_unbound", new Object[0]);
			return remapper.string(k);
		}
		return remapper.string("?");
	}

	@Override
	public String toString(IGui gui) {
		IKeybind rtkb = null;
		for(IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
			if(kb.getName().startsWith(fallback)) {
				rtkb = kb;
			}
		}

		String k = rtkb == null ? "?" : rtkb.getBoundKey();
		if(k.isEmpty())k = gui.i18nFormat("label.cpm.key_unbound");
		return k;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> m = new HashMap<>();
		m.put("kb", name);
		m.put("fallback", fallback);
		return m;
	}
}
