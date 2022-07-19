package com.tom.cpl.gui.util;

import java.util.function.BiConsumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.text.IText;
import com.tom.cpl.text.TextStyle;

public abstract class FormattedTextRenderer implements BiConsumer<String, TextStyle> {
	private int ptr;
	private final IGui gui;
	private final IText text;

	public FormattedTextRenderer(IGui gui, IText text) {
		this.gui = gui;
		this.text = text;
	}

	public void render() {
		text.walkParts(gui, this);
	}

	public int width() {
		text.walkParts(gui, this);
		return ptr;
	}

	@Override
	public void accept(String content, TextStyle style) {
		int w = width(content, style);
		processText(ptr, w, content, style);
		ptr += w;
	}

	public abstract void processText(int x, int w, String content, TextStyle style);
	public abstract int width(String content, TextStyle style);
}