package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.gui.panel.ErrorLogPanel;

public class ErrorLogPopup extends PopupPanel {
	private ErrorLogPanel panel;

	public ErrorLogPopup(Frame frm) {
		super(frm.getGui());

		panel = new ErrorLogPanel(frm, 400, 300);
		addElement(panel);

		setBounds(new Box(0, 0, 400, 300));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("tab.cpm.social.errorLog");
	}
}
