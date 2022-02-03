package com.tom.cpl.text;

import java.util.Map;

import com.tom.cpl.gui.IGui;
import com.tom.cpm.shared.MinecraftCommonAccess;

public interface IText {
	<C> C remap(TextRemapper<C> remapper);
	String toString(IGui gui);
	Map<String, Object> toMap();

	@SuppressWarnings("unchecked")
	default <C> C remap() {
		return (C) remap(MinecraftCommonAccess.get().getTextRemapper());
	}
}
