package com.tom.cpm.shared.gui.gesture;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.math.Box;

public class BindingsInfoPopup extends PopupPanel {
	private String title;

	public BindingsInfoPopup(Frame frame, String title, String text, String scrollTxt) {
		super(frame.getGui());
		this.title = title;

		String[] lines = gui.wordWrap(text, frame.getBounds().w - 200).split("\\\\");
		String[] linesS = gui.wordWrap(scrollTxt, frame.getBounds().w - 200).split("\\\\");

		int wm = 180;

		for (int i = 0; i < lines.length; i++) {
			int w = gui.textWidth(lines[i]);
			if(w > wm)wm = w;
		}
		for (int i = 0; i < linesS.length; i++) {
			int w = gui.textWidth(linesS[i]);
			if(w > wm)wm = w;
		}

		for (int i = 0; i < lines.length; i++) {
			int w = gui.textWidth(lines[i]);
			addElement(new Label(gui, lines[i]).setBounds(new Box(wm / 2 - w / 2 + 10, 5 + i * 10, 0, 0)));
		}

		int mh = frame.getBounds().h - lines.length * 10 - 110;
		mh = Math.max(mh, 20);

		ScrollPanel sc = new ScrollPanel(gui);
		sc.setBounds(new Box(5, 10 + lines.length * 10, wm + 10, mh));
		addElement(sc);

		Panel p = new Panel(gui);
		sc.setDisplay(p);

		for (int i = 0; i < linesS.length; i++) {
			int w = gui.textWidth(linesS[i]);
			p.addElement(new Label(gui, linesS[i]).setBounds(new Box(wm / 2 - w / 2 + 10, 5 + i * 10, 0, 0)));
		}
		p.setBounds(new Box(0, 0, wm - 10, 10 + linesS.length * 10));
		p.setBackgroundColor(gui.getColors().button_fill);

		setBounds(new Box(0, 0, wm + 20, 50 + lines.length * 10 + mh));

		Button ok = new Button(gui, gui.i18nFormat("button.cpm.ok"), this::close);
		ok.setBounds(new Box(wm / 2 - 10, 20 + lines.length * 10 + mh, 40, 20));
		addElement(ok);
	}

	@Override
	public String getTitle() {
		return title;
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
