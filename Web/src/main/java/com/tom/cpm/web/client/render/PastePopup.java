package com.tom.cpm.web.client.render;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.PopupPanel;

public class PastePopup extends PopupPanel {

	public PastePopup(IGui gui) {
		super(gui);
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("web-label.pasteClient");
	}
}
