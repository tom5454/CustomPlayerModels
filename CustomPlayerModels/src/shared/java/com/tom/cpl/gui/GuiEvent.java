package com.tom.cpl.gui;

public class GuiEvent {
	private boolean consumed;

	public boolean isConsumed() {
		return consumed;
	}

	public void consume() {
		this.consumed = true;
	}
}