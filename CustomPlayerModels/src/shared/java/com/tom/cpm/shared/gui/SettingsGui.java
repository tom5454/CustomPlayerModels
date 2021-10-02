package com.tom.cpm.shared.gui;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.gui.panel.SettingsPanel;

public class SettingsGui extends Frame {
	private SettingsPanel panel;

	public SettingsGui(IGui gui) {
		super(gui);
		gui.setCloseListener(c -> {
			if(panel != null && panel.isChanged()) {
				openPopup(new ConfirmPopup(this, gui.i18nFormat("label.cpm.unsaved"), c, null));
			} else c.run();
		});
	}

	@Override
	public void initFrame(int width, int height) {
		panel = new SettingsPanel(this, panel, width / 3 * 2, height / 3 * 2, gui::close);
		panel.setBounds(new Box(width / 6, height / 6, width / 3 * 2, height / 3 * 2));
		addElement(panel);

		Button btn = new Button(gui, "X", gui::close);
		btn.setBounds(new Box(width / 6 + width / 3 * 2 - 20, height / 6, 20, 20));
		addElement(btn);
	}
}
