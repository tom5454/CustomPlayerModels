package com.tom.cpl.text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class StyledText implements IText {
	private final IText text;
	private final TextStyle textStyle;

	public StyledText(IText text, TextStyle textStyle) {
		this.text = text;
		this.textStyle = textStyle;
	}

	@Override
	public <C> C remap(TextRemapper<C> remapper) {
		return remapper.styling(text.remap(remapper), textStyle);
	}

	@Override
	public String toString(I18n gui) {
		return text.toString(gui);
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> m = new HashMap<>();
		m.put("style", textStyle.toMap());
		m.put("text", text.toMap());
		return m;
	}

	@Override
	public void walkParts(I18n gui, BiConsumer<String, TextStyle> consumer) {
		text.walkParts(gui, (c, s) -> consumer.accept(c, textStyle));
	}
}
