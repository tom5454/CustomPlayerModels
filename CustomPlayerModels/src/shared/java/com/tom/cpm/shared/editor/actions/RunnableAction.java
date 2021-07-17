package com.tom.cpm.shared.editor.actions;

public class RunnableAction extends Action {
	public Runnable run, undo;

	public RunnableAction(Runnable run, Runnable undo) {
		this.run = run;
		this.undo = undo;
	}

	public RunnableAction(String name, Runnable run, Runnable undo) {
		super(name);
		this.run = run;
		this.undo = undo;
	}

	@Override
	public void undo() {
		if(undo != null)undo.run();
	}

	@Override
	public void run() {
		if(run != null)run.run();
	}

}
