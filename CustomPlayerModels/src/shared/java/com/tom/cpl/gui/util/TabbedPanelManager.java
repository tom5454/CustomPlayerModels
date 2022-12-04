package com.tom.cpl.gui.util;

import java.util.HashMap;
import java.util.Map;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Panel;

public class TabbedPanelManager extends Panel {
	private Map<Panel, Button> buttons = new HashMap<>();
	public TabbedPanelManager(IGui gui) {
		super(gui);
	}

	public Button createTab(String name, Panel panel) {
		return createTab(name, panel, null);
	}

	public Button createTab(String name, Panel panel, Runnable onSelect) {
		boolean vis = elements.isEmpty();
		panel.setVisible(vis);
		addElement(panel);
		Button btn = new Button(gui, name, null);
		btn.setAction(() -> {
			elements.forEach(p -> p.setVisible(false));
			panel.setVisible(true);
			buttons.values().forEach(b -> b.setEnabled(true));
			btn.setEnabled(false);
			if(onSelect != null)
				onSelect.run();
		});
		btn.setEnabled(!vis);
		buttons.put(panel, btn);
		return btn;
	}

	public void removeTab(Panel panel) {
		elements.remove(panel);
		buttons.remove(panel);
		if(panel.isVisible()) {
			panel.setVisible(false);
			if(!elements.isEmpty()) {
				elements.get(0).setVisible(true);
			}
		}
	}
}
