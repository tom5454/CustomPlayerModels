package com.tom.cpm.blockbench.convert;

import elemental2.promise.Promise;

public class WarnEntry implements Comparable<WarnEntry> {
	private int priority;
	private String message, tooltip;
	private FutureTask autoFix;
	private boolean fixed;

	public static interface FutureTask {
		Promise<Boolean> runTask();
	}

	public WarnEntry(String message) {
		this.message = message;
	}

	public WarnEntry(String message, FutureTask autoFix) {
		this.message = message;
		this.autoFix = autoFix;
	}

	public Promise<Boolean> runQuickFix() {
		return autoFix.runTask().then(s -> {
			fixed = s;
			return Promise.resolve(s);
		});
	}

	public WarnEntry setTooltip(String tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	public WarnEntry setPriority(int priority) {
		this.priority = priority;
		return this;
	}

	public boolean isFixed() {
		return fixed;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public int compareTo(WarnEntry o) {
		return -Integer.compare(priority, o.priority);
	}

	public boolean canQuickFix() {
		return autoFix != null;
	}

	public String getTooltip() {
		return tooltip;
	}
}
