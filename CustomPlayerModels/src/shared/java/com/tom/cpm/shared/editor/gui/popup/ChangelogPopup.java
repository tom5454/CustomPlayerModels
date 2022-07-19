package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.gui.ChangelogPanel;

public class ChangelogPopup extends PopupPanel {

	public ChangelogPopup(IGui gui, String version) {
		super(gui);
		setBounds(new Box(0, 0, 500, 400));

		addElement(new ChangelogPanel(gui, version).setBounds(new Box(5, 5, 490, 390)));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.changelog.title");
	}
}
