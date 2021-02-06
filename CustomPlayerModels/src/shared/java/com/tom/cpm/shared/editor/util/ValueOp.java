package com.tom.cpm.shared.editor.util;

import java.util.function.BiConsumer;

public class ValueOp<E, T> implements Runnable {
	private E elem;
	private BiConsumer<E, T> setter;
	private T value;

	public ValueOp(E elem, T val, BiConsumer<E, T> setter) {
		this.elem = elem;
		this.value = val;
		this.setter = setter;
	}

	@Override
	public void run() {
		setter.accept(elem, value);
	}
}