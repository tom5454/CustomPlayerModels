package com.tom.cpl.text;

import java.util.Map;
import java.util.function.BiConsumer;

import com.tom.cpm.shared.MinecraftCommonAccess;

public interface IText {
	public static final TextStyle NULL = new TextStyle();

	<C> C remap(TextRemapper<C> remapper);
	String toString(I18n gui);
	Map<String, Object> toMap();

	default void walkParts(I18n gui, BiConsumer<String, TextStyle> consumer) {
		consumer.accept(toString(gui), NULL);
	}

	@SuppressWarnings("unchecked")
	default <C> C remap() {
		return (C) remap(MinecraftCommonAccess.get().getTextRemapper());
	}
}
