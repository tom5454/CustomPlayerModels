package com.tom.cpm.shared.editor.gui.popup;

import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.definition.Link;

public class CreateGistPopup extends PopupPanel {
	public CreateGistPopup(Frame frm, IGui gui, String reason, String text, Consumer<Link> ok) {
		super(gui);

		setBounds(new Box(0, 0, 260, 120));

		Label lbl1 = new Label(gui, gui.i18nFormat("label.cpm." + reason));
		lbl1.setBounds(new Box(5, 0, 0, 0));
		addElement(lbl1);

		Label lbl2 = new Label(gui, gui.i18nFormat("label.cpm.createGist"));
		lbl2.setBounds(new Box(5, 10, 0, 0));
		addElement(lbl2);

		TextField txtf = new TextField(gui);
		txtf.setText(text);
		txtf.setBounds(new Box(5, 20, 205, 20));
		addElement(txtf);

		Label lbl3 = new Label(gui, gui.i18nFormat("label.cpm.uploadURL"));
		lbl3.setBounds(new Box(5, 45, 0, 0));
		addElement(lbl3);

		Label lbl4 = new Label(gui, "");

		TextField urlf = new TextField(gui);
		urlf.setBounds(new Box(5, 55, 205, 20));
		urlf.setEventListener(() -> checkURL(frm, urlf.getText(), lbl4, null));
		addElement(urlf);

		lbl4.setBounds(new Box(5, 80, 250, 10));
		addElement(lbl4);

		Button okBtn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> checkURL(frm, urlf.getText(), lbl4, l -> {
			ok.accept(l);
			close();
		}));
		okBtn.setBounds(new Box(110, 90, 40, 20));
		addElement(okBtn);

		Button cpyBtn = new Button(gui, gui.i18nFormat("button.cpm.copy"), () -> gui.setClipboardText(text));
		cpyBtn.setBounds(new Box(215, 20, 40, 20));
		addElement(cpyBtn);

		Button pasteBtn = new Button(gui, gui.i18nFormat("button.cpm.paste"), () -> {
			urlf.setText(gui.getClipboardText());
			checkURL(frm, urlf.getText(), lbl4, null);
		});
		pasteBtn.setBounds(new Box(215, 55, 40, 20));
		addElement(pasteBtn);
	}

	private void checkURL(Frame frm, String text, Label setter, Consumer<Link> linkConsumer) {
		if(text.isEmpty()) {
			setter.setText("");
			setter.setTooltip(null);
			return;
		}
		if(text.startsWith("https://gist.github.com/")) {
			text = text.substring("https://gist.github.com/".length());
			String[] sp = text.split("/");
			if(sp.length < 2) {
				setter.setText("Inavlid Gist URL");
				setter.setTooltip(new Tooltip(frm, "Your link should look like this:\\https://gist.github.com/<name>/<gist>"));
				return;
			} else {
				setter.setText("");
				setter.setTooltip(null);
				if(linkConsumer != null)
					linkConsumer.accept(new Link("git", sp[0] + "/" + sp[1]));
				return;
			}
		}
		setter.setText("Unknown URL");
		setter.setTooltip(new Tooltip(frm, "Unknown link, currently only GitHub Gists are supported."));
		return;
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.exportSkin");
	}
}
