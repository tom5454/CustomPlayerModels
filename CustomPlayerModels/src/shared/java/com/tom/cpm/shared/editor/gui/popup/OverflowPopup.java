package com.tom.cpm.shared.editor.gui.popup;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.HorizontalLayout;
import com.tom.cpl.gui.util.TabbedPanelManager;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.paste.PastePopup;

public class OverflowPopup extends PopupPanel {
	private HorizontalLayout topPanel;
	private TabbedPanelManager tabs;
	private String reason;

	public OverflowPopup(EditorGui frm, String text, String reason, Consumer<Link> ok) {
		super(frm.getGui());
		this.reason = reason;

		setBounds(new Box(0, 0, 260, 140));

		tabs = new TabbedPanelManager(gui);
		tabs.setBounds(new Box(0, 20, bounds.w, bounds.h - 20));
		addElement(tabs);

		Panel topPanel = new Panel(gui);
		topPanel.setBounds(new Box(0, 0, bounds.w, 20));
		topPanel.setBackgroundColor(gui.getColors().menu_bar_background);
		addElement(topPanel);
		this.topPanel = new HorizontalLayout(topPanel);

		{
			Panel paste = new Panel(gui);
			addTab("paste", paste, 5);

			Label lbl1 = new Label(gui, gui.i18nFormat("label.cpm." + reason + "Overflow"));
			lbl1.setBounds(new Box(5, 0, 0, 0));
			paste.addElement(lbl1);

			Label lbl2 = new Label(gui, gui.i18nFormat("label.cpm.paste.name"));
			lbl2.setBounds(new Box(5, 10, 0, 0));
			paste.addElement(lbl2);

			TextField txtf = new TextField(gui);
			File file = frm.getEditor().file;
			String name = file == null ? gui.i18nFormat("label.cpm.new_project") : file.getName();
			txtf.setText(name);
			txtf.setBounds(new Box(5, 20, 205, 20));
			paste.addElement(txtf);

			Button okBtn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
				String fn = txtf.getText();
				PastePopup.runRequest(frm, c -> c.uploadFile(fn, text.getBytes(StandardCharsets.UTF_8)),
						id -> ok.accept(new Link("p", id)), () -> {}, "uploading");
				close();
			});
			okBtn.setBounds(new Box(110, 70, 40, 20));
			paste.addElement(okBtn);
		}

		{
			Panel gist = new Panel(gui);
			addTab("gist", gist, 5);

			Label lbl1 = new Label(gui, gui.i18nFormat("label.cpm." + reason + "Overflow"));
			lbl1.setBounds(new Box(5, 0, 0, 0));
			gist.addElement(lbl1);

			Label lbl2 = new Label(gui, gui.i18nFormat("label.cpm.createGist"));
			lbl2.setBounds(new Box(5, 10, 0, 0));
			gist.addElement(lbl2);

			TextField txtf = new TextField(gui);
			txtf.setText(text);
			txtf.setBounds(new Box(5, 20, 205, 20));
			gist.addElement(txtf);

			Label lbl3 = new Label(gui, gui.i18nFormat("label.cpm.uploadURL"));
			lbl3.setBounds(new Box(5, 45, 0, 0));
			gist.addElement(lbl3);

			Label lbl4 = new Label(gui, "");

			TextField urlf = new TextField(gui);
			urlf.setBounds(new Box(5, 55, 205, 20));
			urlf.setEventListener(() -> checkURL(frm, urlf.getText(), lbl4, null));
			gist.addElement(urlf);

			lbl4.setBounds(new Box(5, 80, 250, 10));
			gist.addElement(lbl4);

			Button okBtn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> checkURL(frm, urlf.getText(), lbl4, l -> {
				ok.accept(l);
				close();
			}));
			okBtn.setBounds(new Box(110, 80, 40, 20));
			gist.addElement(okBtn);

			Button cpyBtn = new Button(gui, gui.i18nFormat("button.cpm.copy"), () -> gui.setClipboardText(text));
			cpyBtn.setBounds(new Box(215, 20, 40, 20));
			gist.addElement(cpyBtn);

			Button pasteBtn = new Button(gui, gui.i18nFormat("button.cpm.paste"), () -> {
				urlf.setText(gui.getClipboardText());
				checkURL(frm, urlf.getText(), lbl4, null);
			});
			pasteBtn.setBounds(new Box(215, 55, 40, 20));
			gist.addElement(pasteBtn);
		}
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
		return gui.i18nFormat("label.cpm.export" + Character.toUpperCase(reason.charAt(0)) + reason.substring(1));
	}

	public void addTab(String name, Panel panel, int topPadding) {
		panel.setBounds(new Box(0, topPadding, bounds.w, bounds.h - 20));
		topPanel.add(tabs.createTab(gui.i18nFormat("tab.cpm.export." + name), panel));
	}
}
