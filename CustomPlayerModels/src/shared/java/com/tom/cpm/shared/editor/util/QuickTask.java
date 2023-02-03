package com.tom.cpm.shared.editor.util;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.gui.Keybinds;

public class QuickTask {
	protected final String name, tooltip;
	protected final Runnable run;

	public QuickTask(String name, String tooltip, Runnable run) {
		this.name = name;
		this.tooltip = tooltip;
		this.run = run;
	}

	public void initButtons(IGui gui, Panel p) {
		Button b = new Button(gui, name + " (" + Keybinds.RUN_QUICK_ACTION.getSetKey(gui) + ")", run);
		if(tooltip != null)b.setTooltip(new Tooltip(gui.getFrame(), tooltip));
		b.setBounds(new Box(0, 0, p.getBounds().w, 20));
		p.addElement(b);
	}

	public void runTask() {
		run.run();
	}
}
