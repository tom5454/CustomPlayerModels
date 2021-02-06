package com.tom.cpm.shared.gui.elements;

import com.tom.cpm.shared.gui.Frame;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.math.Box;

public class PopupMenu extends PopupPanel {

	public PopupMenu(IGui gui) {
		super(gui);
	}

	public Button addButton(String name, Runnable r) {
		Button btn = new Button(gui, name, r);
		btn.setBounds(new Box(0, elements.size() * 20, 80, 20));
		addElement(btn);
		return btn;
	}

	public void add(GuiElement elem) {
		if(elem.bounds == null)elem.bounds = new Box(0, 0, 0, 0);
		elem.setBounds(new Box(elem.bounds.x, elem.bounds.y + elements.size() * 20, 80, 20));
		addElement(elem);
	}

	public void display(Frame frame, int x, int y) {
		display(frame, x, y, 80);
	}

	public void display(Frame frame, int x, int y, int w) {
		for (GuiElement elem : elements) {
			if(elem instanceof Button) {
				w = Math.max(w, gui.textWidth(((Button)elem).getText()) + 20);
			} else if(elem instanceof Checkbox) {
				w = Math.max(w, gui.textWidth(((Checkbox)elem).getText()) + 30);
			}
		}
		for (GuiElement elem : elements) {
			Box b = elem.getBounds();
			elem.setBounds(new Box(b.x, b.y, w, b.h));
		}
		setBounds(new Box(x, y, w, elements.size() * 20));
		frame.openPopup(this);
	}

	@Override
	public void mouseClick(MouseEvent event) {
		close();
		super.mouseClick(event);
	}

	@Override
	public boolean hasDecoration() {
		return false;
	}
}
