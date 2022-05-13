package com.tom.cpl.gui.elements;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.math.Box;

public class PopupMenu extends PopupPanel {
	private int x, y;
	private Frame frame;
	private ScrollPanel scroll;
	private Panel panel;

	public PopupMenu(IGui gui, Frame frm) {
		super(gui);
		this.frame = frm;
		this.scroll = new ScrollPanel(gui);
		this.panel = new Panel(gui);
		scroll.setDisplay(panel);
		addElement(scroll);
	}

	public Button addButton(String name, Runnable r) {
		Button btn = new Button(gui, name, () -> {
			r.run();
			close();
		});
		btn.setBounds(new Box(0, panel.elements.size() * 20, 80, 20));
		panel.addElement(btn);
		return btn;
	}

	public Button addMenuButton(String name, PopupMenu menu) {
		int y = panel.elements.size() * 20;
		Button btn = new Button(gui, name, null);
		btn.setAction(() -> {
			menu.display(x + getBounds().w, this.y + y - scroll.getScrollY());
			menu.setOnClosed(this::close);
		});
		btn.setBounds(new Box(0, y, 80, 20));
		panel.addElement(btn);
		return btn;
	}

	public Button addMenuButton(String name, Supplier<PopupMenu> menuIn) {
		int y = panel.elements.size() * 20;
		Button btn = new Button(gui, name, null);
		btn.setAction(() -> {
			PopupMenu menu = menuIn.get();
			menu.display(x + getBounds().w, this.y + y - scroll.getScrollY());
			menu.setOnClosed(this::close);
		});
		btn.setBounds(new Box(0, y, 80, 20));
		panel.addElement(btn);
		return btn;
	}

	public Checkbox addCheckbox(String name, Runnable r) {
		Checkbox box = new Checkbox(gui, name);
		box.setAction(() -> {
			r.run();
			close();
		});
		box.setBounds(new Box(0, panel.elements.size() * 20, 80, 20));
		panel.addElement(box);
		return box;
	}

	public Checkbox addCheckbox(String name, Consumer<Checkbox> r) {
		Checkbox box = new Checkbox(gui, name);
		box.setAction(() -> {
			r.accept(box);
			close();
		});
		box.setBounds(new Box(0, panel.elements.size() * 20, 80, 20));
		panel.addElement(box);
		return box;
	}

	public void add(GuiElement elem) {
		if(elem.bounds == null)elem.bounds = new Box(0, 0, 0, 0);
		elem.setBounds(new Box(elem.bounds.x, elem.bounds.y + panel.elements.size() * 20, 80, 20));
		panel.addElement(elem);
	}

	public void display(int x, int y) {
		display(x, y, 80);
	}

	public void display(int x, int y, int w) {
		this.x = x;
		this.y = y;
		for (GuiElement elem : panel.elements) {
			if(elem instanceof Button) {
				w = Math.max(w, gui.textWidth(((Button)elem).getText()) + 20);
			} else if(elem instanceof Checkbox) {
				w = Math.max(w, gui.textWidth(((Checkbox)elem).getText()) + 30);
			}
		}
		for (GuiElement elem : panel.elements) {
			Box b = elem.getBounds();
			elem.setBounds(new Box(b.x, b.y, w, b.h));
		}
		int h = panel.elements.size() * 20;
		int ph = Math.min(h, frame.getBounds().h - y);
		panel.setBounds(new Box(0, 0, w, h));
		setBounds(new Box(x, y, w, ph));
		scroll.setBounds(new Box(0, 0, w, ph));
		frame.openPopup(this);
	}

	@Override
	public void mouseClick(MouseEvent event) {
		if(!event.isConsumed() && !event.isInBounds(bounds))close();
		super.mouseClick(event);
	}

	@Override
	public boolean hasDecoration() {
		return false;
	}

	public int getY() {
		return panel.elements.size() * 20;
	}
}
