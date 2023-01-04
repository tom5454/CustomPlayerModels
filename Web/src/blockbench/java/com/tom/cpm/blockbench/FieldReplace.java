package com.tom.cpm.blockbench;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class FieldReplace<O, T> implements Runnable {
	private O object;
	private T old;
	private BiConsumer<O, T> set;

	public FieldReplace(O obj, Function<O, T> get, BiConsumer<O, T> set, T replace) {
		this(obj, get, set, replace, (a, b) -> b);
	}

	public FieldReplace(O obj, Function<O, T> get, BiConsumer<O, T> set, T replace, BinaryOperator<T> combine) {
		old = get.apply(obj);
		this.object = obj;
		this.set = set;
		T v;
		if(old != null) {
			v = combine.apply(old, replace);
		} else {
			v = replace;
		}
		set.accept(obj, v);
	}

	@Override
	public void run() {
		set.accept(object, old);
	}
}
