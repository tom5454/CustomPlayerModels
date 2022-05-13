package com.tom.cpm.shared.gui;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.config.Keybind;

public class KeybindPanel extends Panel {
	private Keybind kb;
	private ConfigEntry ce;
	private Button btn;

	public KeybindPanel(Frame frm, Keybind kb, ConfigEntry ce) {
		super(frm.getGui());
		this.kb = kb;
		this.ce = ce;

		setBounds(new Box(0, 0, 350, 20));

		addElement(new Label(gui, gui.i18nFormat(kb.getKeyName())).setBounds(new Box(5, 5, 100, 10)));

		btn = new Button(gui, "", () -> {
			KeybindPopup kbp = new KeybindPopup(frm, ce, kb);
			kbp.setOnClosed(this::updateButton);
			frm.openPopup(kbp);
		});
		btn.setBounds(new Box(180, 0, 100, 20));
		addElement(btn);
		updateButton();

		Button btnR = new Button(gui, "Reset", () -> {
			kb.resetKey(ce);
			updateButton();
		});
		btnR.setBounds(new Box(285, 0, 60, 20));
		addElement(btnR);
	}

	private void updateButton() {
		btn.setText(kb.getSetKey(ce, gui));
	}
}
