package com.tom.cpl.util;

@FunctionalInterface
public interface ThrowingSupplier<T, X extends Throwable> {
	T get() throws X;
}
