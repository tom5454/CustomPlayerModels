package com.tom.cpm.shared.gui.elements;

import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.math.Box;

public class MessagePopup extends PopupPanel {
	private String title;

	public MessagePopup(IGui gui, String title, String text) {
		super(gui);
		this.title = title;
		String[] lines = text.split("\\\\");

		int wm = 180;

		for (int i = 0; i < lines.length; i++) {
			int w = gui.textWidth(lines[i]);
			if(w > wm)wm = w;
		}

		for (int i = 0; i < lines.length; i++) {
			int w = gui.textWidth(lines[i]);
			addElement(new Label(gui, lines[i]).setBounds(new Box(wm / 2 - w / 2 + 10, 5 + i * 10, 0, 0)));
		}
		setBounds(new Box(0, 0, wm + 20, 45 + lines.length * 10));

		Button ok = new Button(gui, gui.i18nFormat("button.cpm.ok"), this::close);
		ok.setBounds(new Box(wm / 2 - 10, 20 + lines.length * 10, 40, 20));
		addElement(ok);
	}

	@Override
	public String getTitle() {
		return title;
	}
}
