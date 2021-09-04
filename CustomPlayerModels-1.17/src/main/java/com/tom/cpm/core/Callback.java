package com.tom.cpm.core;

public class Callback {
	private boolean cancelled;

	public boolean isCancelled() {
		return cancelled;
	}

	public void cancel() {
		this.cancelled = true;
	}

	public static Callback create() {
		return new Callback();
	}

	public static <T> CallbackReturnable<T> createReturnable() {
		return new CallbackReturnable<>();
	}

	public static <T> CallbackReturnable<T> createReturnable(T value) {
		return new CallbackReturnable<>(value);
	}
}
