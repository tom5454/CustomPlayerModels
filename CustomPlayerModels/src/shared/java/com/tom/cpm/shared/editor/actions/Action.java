package com.tom.cpm.shared.editor.actions;

public abstract class Action {
	private final String name;

	public Action(String name) {
		this.name = name;
	}

	public Action() {
		name = "";
	}

	public abstract void undo();
	public abstract void run();

	public String getName() {
		return name;
	}
}
