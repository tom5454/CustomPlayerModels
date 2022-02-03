package com.tom.cpl.text;

import java.util.Collections;
import java.util.Map;

import com.tom.cpl.gui.IGui;

public class LiteralText implements IText {
	private String value;

	public LiteralText(String value) {
		this.value = value;
	}

	@Override
	public <C> C remap(TextRemapper<C> remapper) {
		return remapper.string(value);
	}

	@Override
	public String toString(IGui gui) {
		return value;
	}

	@Override
	public Map<String, Object> toMap() {
		return Collections.singletonMap("text", value);
	}

	@Override
	public String toString() {
		return value;
	}
}
