package com.tom.cpm.shared.gui.elements;

import com.tom.cpm.shared.gui.Frame;
import com.tom.cpm.shared.math.Box;

public class Tooltip extends Panel {
	private final Frame frm;
	public Tooltip(Frame frm) {
		super(frm.getGui());
		this.frm = frm;
		setBackgroundColor(gui.getColors().panel_background);
	}

	public Tooltip(Frame frm, String text) {
		this(frm);
		String[] lines = text.split("\\\\");

		int wm = 180;
		for (int i = 0; i < lines.length; i++) {
			int w = gui.textWidth(lines[i]);
			if(w > wm)wm = w;
		}

		for (int i = 0; i < lines.length; i++) {
			addElement(new Label(gui, lines[i]).setBounds(new Box(5, 5 + i * 10, 0, 0)));
		}
		setBounds(new Box(0, 0, wm + 20, 8 + lines.length * 10));
	}

	public void set() {
		frm.setTooltip(this);
	}
}
