package com.tom.cpl.function;

@FunctionalInterface
public interface ToFloatFunction<P> {
	float apply(P v);
}