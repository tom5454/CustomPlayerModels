package com.tom.cpl.gui;

public class KeyboardEvent extends GuiEvent {
	public int keyCode, scancode;
	public char charTyped;
	public String keyName;

	public KeyboardEvent(int keyCode, int scancode, char charTyped, String keyName) {
		this.keyCode = keyCode;
		this.scancode = scancode;
		this.charTyped = charTyped;
		this.keyName = keyName;
	}

	public boolean matches(int keyCode) {
		return !isConsumed() && this.keyCode == keyCode;
	}

	public boolean matches(String keyName) {
		return !isConsumed() && this.keyName != null && this.keyName.equalsIgnoreCase(keyName);
	}
}