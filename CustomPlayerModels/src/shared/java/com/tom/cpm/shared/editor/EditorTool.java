package com.tom.cpm.shared.editor;

import java.util.function.Consumer;

import com.tom.cpl.gui.elements.GuiElement;

public enum EditorTool {
	PEN,
	RUBBER,
	MOVE_UV
	;
	public static final EditorTool[] VALUES = values();

	public Consumer<EditorTool> setEnabled(GuiElement elem) {
		return t -> elem.setEnabled(t != this);
	}
}
