package com.tom.cpl.gui;

public interface IKeybind {
	boolean isPressed(KeyboardEvent evt);
	String getBoundKey();
	String getName();
}
