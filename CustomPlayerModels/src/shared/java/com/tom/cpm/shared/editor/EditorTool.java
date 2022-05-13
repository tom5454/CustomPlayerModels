package com.tom.cpm.shared.editor;

import java.util.function.Consumer;

import com.tom.cpl.gui.elements.GuiElement;

public enum EditorTool {
	SELECT,
	PEN,
	RUBBER,
	MOVE_UV,
	FILL,
	COLOR_PICKER,
	;
	public static final EditorTool[] VALUES = values();

	public Consumer<EditorTool> setEnabled(GuiElement elem) {
		return t -> elem.setEnabled(t != this);
	}
}
