package com.tom.cpm.blockbench.convert;

import java.util.List;

import com.tom.cpm.web.client.util.I18n;

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

	public static class MultiWarnEntry extends WarnEntry {
		private int count = 1;

		private MultiWarnEntry(String message) {
			super(message);
		}

		public static WarnEntry addOrIncEntry(List<WarnEntry> ent, String msg) {
			WarnEntry e = ent.stream().filter(w -> w instanceof MultiWarnEntry && ((MultiWarnEntry)w).getMessage0().equals(msg)).findFirst().orElse(null);
			if(e != null) {
				((MultiWarnEntry)e).count++;
			} else {
				e = new MultiWarnEntry(msg);
				ent.add(e);
			}
			return e;
		}

		public String getMessage0() {
			return super.getMessage();
		}

		@Override
		public String getMessage() {
			return I18n.formatBr("bb-label.warn.multiple", super.getMessage(), count);
		}
	}
}
