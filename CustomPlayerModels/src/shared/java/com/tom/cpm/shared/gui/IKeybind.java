package com.tom.cpm.shared.gui;

import com.tom.cpm.shared.gui.Gui.KeyboardEvent;

public interface IKeybind {
	boolean isPressed(KeyboardEvent evt);
	String getBoundKey();
	String getName();
}
