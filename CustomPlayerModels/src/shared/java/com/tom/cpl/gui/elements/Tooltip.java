package com.tom.cpl.gui.elements;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.math.Box;

public class Tooltip extends Panel {
	private final Frame frm;
	public Tooltip(Frame frm) {
		super(frm.getGui());
		this.frm = frm;
		setBackgroundColor(gui.getColors().popup_border);
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

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		gui.pushMatrix();
		gui.setPosOffset(getBounds());
		Box bounds = getBounds();
		gui.drawBox(0, 0, bounds.w, bounds.h, backgroundColor);
		gui.drawBox(1, 1, bounds.w - 2, bounds.h - 2, gui.getColors().panel_background);
		for (GuiElement guiElement : elements) {
			if(guiElement.isVisible())
				guiElement.draw(mouseX - bounds.x, mouseY - bounds.y, partialTicks);
		}
		gui.popMatrix();
	}
}
