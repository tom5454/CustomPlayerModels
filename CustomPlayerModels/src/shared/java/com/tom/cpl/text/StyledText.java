package com.tom.cpl.text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.tom.cpl.gui.IGui;

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
	public String toString(IGui gui) {
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
	public void walkParts(IGui gui, BiConsumer<String, TextStyle> consumer) {
		text.walkParts(gui, (c, s) -> consumer.accept(c, textStyle));
	}
}
