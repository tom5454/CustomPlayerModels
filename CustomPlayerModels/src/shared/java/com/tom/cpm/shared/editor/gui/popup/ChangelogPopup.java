package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.gui.ChangelogPanel;

public class ChangelogPopup extends PopupPanel {
	private ChangelogPanel cp;

	public ChangelogPopup(IGui gui, String version) {
		super(gui);
		setBounds(new Box(0, 0, 100, 100));

		cp = new ChangelogPanel(gui, version);
		addElement(cp.setBounds(new Box(5, 5, 100, 100)));
		onInit();
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.changelog.title");
	}

	@Override
	public void onInit() {
		Box b = gui.getFrame().getBounds();
		int w = b.w * 2 / 3;
		int h = b.h * 2 / 3;
		setBounds(new Box(0, 0, w, h));
		cp.setBounds(new Box(5, 5, w - 10, h - 10));
	}
}
