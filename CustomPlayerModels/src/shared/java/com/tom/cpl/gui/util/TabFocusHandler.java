package com.tom.cpl.gui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.math.Box;

public class TabFocusHandler extends GuiElement {
	public List<Focusable> focusables = new ArrayList<>();
	private IGui gui;

	public TabFocusHandler(IGui gui) {
		super(gui);
		this.gui = gui;
		setBounds(new Box(0, 0, 0, 0));
	}

	public interface Focusable {
		boolean isFocused();
		void setFocused(boolean focused);
		boolean isVisible();
	}

	public boolean add(Focusable e) {
		return focusables.add(e);
	}

	public boolean remove(Focusable o) {
		return focusables.remove(o);
	}

	@Override
	public void keyPressed(KeyboardEvent e) {
		if(e.matches(gui.getKeyCodes().KEY_TAB)) {
			List<Focusable> fc = focusables.stream().filter(Focusable::isVisible).collect(Collectors.toList());
			int id = -1;
			for (int i = 0; i < fc.size(); i++) {
				Focusable f = fc.get(i);
				if(f.isFocused()) {
					id = i;
					e.consume();
					break;
				}
			}
			if(id != -1) {
				id = (id + 1) % fc.size();
				for (int i = 0; i < fc.size(); i++) {
					Focusable f = fc.get(i);
					f.setFocused(i == id);
				}
			}
		}
	}
}
