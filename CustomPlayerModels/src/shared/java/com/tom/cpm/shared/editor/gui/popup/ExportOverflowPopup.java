package com.tom.cpm.shared.editor.gui.popup;

import java.util.function.Consumer;

import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.Label;
import com.tom.cpm.shared.gui.elements.PopupPanel;
import com.tom.cpm.shared.gui.elements.TextField;
import com.tom.cpm.shared.math.Box;

public class ExportOverflowPopup extends PopupPanel {
	public ExportOverflowPopup(IGui gui, String b64, Consumer<Link> ok) {
		super(gui);

		setBounds(new Box(0, 0, 260, 120));

		Label lbl1 = new Label(gui, gui.i18nFormat("label.cpm.skinOverflow"));
		lbl1.setBounds(new Box(5, 0, 0, 0));
		addElement(lbl1);

		Label lbl2 = new Label(gui, gui.i18nFormat("label.cpm.skinOverflow.desc"));
		lbl2.setBounds(new Box(5, 10, 0, 0));
		addElement(lbl2);

		TextField txtf = new TextField(gui);
		txtf.setText(b64);
		txtf.setBounds(new Box(5, 20, 205, 20));
		addElement(txtf);

		Label lbl3 = new Label(gui, gui.i18nFormat("label.cpm.uploadURL"));
		lbl3.setBounds(new Box(5, 45, 0, 0));
		addElement(lbl3);

		Label lbl4 = new Label(gui, "");

		TextField urlf = new TextField(gui);
		urlf.setBounds(new Box(5, 55, 250, 20));
		urlf.setEventListener(() -> checkURL(urlf.getText(), lbl4::setText, null));
		addElement(urlf);

		lbl4.setBounds(new Box(5, 80, 0, 0));
		addElement(lbl4);

		Button okBtn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> checkURL(urlf.getText(), lbl4::setText, l -> {
			ok.accept(l);
			close();
		}));
		okBtn.setBounds(new Box(110, 90, 40, 20));
		addElement(okBtn);

		Button cpyBtn = new Button(gui, gui.i18nFormat("button.cpm.copy"), () -> gui.setClipboardText(b64));
		cpyBtn.setBounds(new Box(215, 20, 40, 20));
		addElement(cpyBtn);
	}

	private boolean checkURL(String text, Consumer<String> setter, Consumer<Link> linkConsumer) {
		if(text.startsWith("https://gist.github.com/")) {
			text = text.substring("https://gist.github.com/".length());
			String[] sp = text.split("/");
			if(sp.length < 2) {
				setter.accept("Unknown URL");
				return false;
			} else {
				setter.accept("");
				if(linkConsumer != null)
					linkConsumer.accept(new Link("git", sp[0] + "/" + sp[1]));
				return true;
			}
		}
		setter.accept("Unknown URL");
		return false;
	}
}
