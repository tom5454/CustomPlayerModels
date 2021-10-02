package com.tom.cpl.util;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U, X extends Throwable> {
	void accept(T t, U u) throws X;
}
