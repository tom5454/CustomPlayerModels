package com.tom.cpl.util;

@FunctionalInterface
public interface ThrowingRunnable<X extends Throwable> {
	public void run() throws X;
}
