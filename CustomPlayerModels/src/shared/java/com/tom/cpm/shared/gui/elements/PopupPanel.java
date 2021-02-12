package com.tom.cpm.shared.gui.elements;

import com.tom.cpm.shared.gui.Frame.PopupLayer;
import com.tom.cpm.shared.gui.IGui;

public class PopupPanel extends Panel {
	private Runnable onClosed;
	protected PopupLayer layer;

	public PopupPanel(IGui gui) {
		super(gui);
	}

	public void onClosed() {
		if(onClosed != null)onClosed.run();
	}

	public void setOnClosed(Runnable onClosed) {
		this.onClosed = onClosed;
	}

	public boolean hasDecoration() {
		return true;
	}

	public void close() {
		layer.close();
	}

	public void setLayer(PopupLayer layer) {
		this.layer = layer;
	}

	public boolean onEscape() {
		close();
		return true;
	}

	public String getTitle() {
		return "";
	}
}
