package com.tom.cpl.gui.elements;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.math.Box;

public class MessagePopup extends PopupPanel implements Runnable {
	private String title;
	private Frame frame;

	public MessagePopup(Frame frame, String title, String text) {
		this(frame, title, text, frame.gui.i18nFormat("button.cpm.ok"));
	}

	public MessagePopup(Frame frame, String title, String text, String closeBtn) {
		super(frame.getGui());
		this.frame = frame;
		this.title = title;
		String[] lines = gui.wordWrap(text, frame.getBounds().w - 200).split("\\\\");

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

		Button ok = new Button(gui, closeBtn, this::close);
		ok.setBounds(new Box(wm / 2 - 10, 20 + lines.length * 10, 40, 20));
		addElement(ok);
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void run() {
		frame.openPopup(this);
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		if(event.matches(gui.getKeyCodes().KEY_ENTER)) {
			close();
			event.consume();
		}
		super.keyPressed(event);
	}
}
