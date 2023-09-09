package com.tom.cpm.shared.editor.util;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.gui.Keybinds;

public class QuickTask {
	private List<Task> tasks;
	private boolean noAct;

	public QuickTask(String name, String tooltip, Runnable run) {
		tasks = new ArrayList<>();
		tasks.add(new Task(name, tooltip, run));
	}

	public QuickTask(String name, String tooltip, Runnable run, boolean noAct) {
		tasks = new ArrayList<>();
		tasks.add(new Task(name, tooltip, run));
		this.noAct = noAct;
	}

	public QuickTask add(String name, String tooltip, Runnable run) {
		tasks.add(new Task(name, tooltip, run));
		return this;
	}

	public void initButtons(IGui gui, Panel p) {
		for (int i = 0; i < tasks.size(); i++) {
			Task t = tasks.get(i);
			String name = t.name;
			if (i == 0 && !noAct)name += " (" + Keybinds.RUN_QUICK_ACTION.getSetKey(gui) + ")";
			Button b = new Button(gui, name, t.action);
			if(t.tooltip != null)b.setTooltip(new Tooltip(gui.getFrame(), t.tooltip));
			b.setBounds(new Box(0, 0, p.getBounds().w, 20));
			p.addElement(b);
		}
	}

	public void runTask() {
		if(!noAct)
			tasks.get(0).action.run();
	}

	private static class Task {
		public final String name, tooltip;
		public final Runnable action;

		public Task(String name, String tooltip, Runnable run) {
			this.name = name;
			this.tooltip = tooltip;
			this.action = run;
		}
	}
}
