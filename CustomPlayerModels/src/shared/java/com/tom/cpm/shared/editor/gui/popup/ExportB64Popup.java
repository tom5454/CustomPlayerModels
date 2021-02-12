package com.tom.cpm.shared.editor.gui.popup;

import com.tom.cpm.shared.gui.Frame;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.Label;
import com.tom.cpm.shared.gui.elements.PopupPanel;
import com.tom.cpm.shared.gui.elements.TextField;
import com.tom.cpm.shared.math.Box;

public class ExportB64Popup extends PopupPanel {
	public ExportB64Popup(Frame frm, IGui gui, String b64) {
		super(gui);

		setBounds(new Box(0, 0, 260, 80));

		Label lbl1 = new Label(gui, gui.i18nFormat("label.cpm.base64_model"));
		lbl1.setBounds(new Box(5, 0, 0, 0));
		addElement(lbl1);

		Label lbl2 = new Label(gui, gui.i18nFormat("label.cpm.base64_model.desc"));
		lbl2.setBounds(new Box(5, 10, 0, 0));
		addElement(lbl2);

		TextField txtf = new TextField(gui);
		txtf.setText(b64);
		txtf.setBounds(new Box(5, 20, 205, 20));
		addElement(txtf);

		Button okBtn = new Button(gui, gui.i18nFormat("button.cpm.ok"), this::close);
		okBtn.setBounds(new Box(110, 50, 40, 20));
		addElement(okBtn);

		Button cpyBtn = new Button(gui, gui.i18nFormat("button.cpm.copy"), () -> gui.setClipboardText(b64));
		cpyBtn.setBounds(new Box(215, 20, 40, 20));
		addElement(cpyBtn);
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.exportSkin");
	}
}
