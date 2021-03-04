package com.tom.cpm.shared.gui.util;

import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.GuiElement;
import com.tom.cpm.shared.gui.elements.Panel;
import com.tom.cpm.shared.math.Box;

public class HorizontalLayout {
	private int x;
	private Panel panel;

	public HorizontalLayout(Panel panel) {
		this.panel = panel;
	}

	public void add(GuiElement elem) {
		Box b = elem.getBounds();
		if(b == null)b = new Box(0, 0, 0, 0);
		if(elem instanceof Button) {
			if(b.w == 0)b.w = panel.getGui().textWidth(((Button) elem).getText()) + 16;
			if(b.h == 0)b.h = 20;
		}
		elem.setBounds(new Box(x + b.x, b.y, b.w, b.h));
		x += b.x;
		x += b.w;
		panel.addElement(elem);
	}

	public int getX() {
		return x;
	}

	public void addX(int i) {
		x += i;
	}
}
