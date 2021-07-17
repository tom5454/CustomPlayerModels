package com.tom.cpl.gui.util;

import java.util.List;

import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.math.Box;

public class FlowLayout implements Runnable {
	private Panel panel;
	private int padding;
	private int direction;
	private int size;

	public FlowLayout(Panel panel, int padding, int direction) {
		this.panel = panel;
		this.padding = padding;
		this.direction = direction;
	}

	public void reflow() {
		if(direction == 0) {
			List<GuiElement> elements = panel.getElements();
			int space = (size - padding) / elements.size();
			for (int i = 0; i < elements.size(); i++) {
				GuiElement e = elements.get(i);
				Box b = e.getBounds();
				e.setBounds(new Box(i * space + padding, b.y, space - padding, b.h));
			}
		} else {
			List<GuiElement> elements = panel.getElements();
			int h = 0;
			for (int i = 0; i < elements.size(); i++) {
				GuiElement e = elements.get(i);
				if(e.isVisible()) {
					Box b = e.getBounds();
					e.setBounds(new Box(b.x, h, b.w, b.h));
					h += b.h;
					h += padding;
				}
			}
			Box b = panel.getBounds();
			panel.setBounds(new Box(b.x, b.y, b.w, h));
		}
	}

	public void setSize(int size) {
		this.size = size;
		reflow();
	}

	@Override
	public void run() {
		reflow();
	}
}
