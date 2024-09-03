package com.tom.cpl.gui;

public interface IKeybind {
	public static final int QUICK_ACCESS_KEYBINDS_COUNT = 16;

	boolean isPressed(KeyboardEvent evt);
	String getBoundKey();
	String getName();
	boolean isPressed();
}
