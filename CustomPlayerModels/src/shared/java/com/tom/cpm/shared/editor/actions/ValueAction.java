package com.tom.cpm.shared.editor.actions;

import java.util.function.BiConsumer;

public class ValueAction<E, T> extends Action {
	private E elem;
	private BiConsumer<E, T> setter;
	private T newValue;
	private T oldValue;

	public ValueAction(String name, E elem, BiConsumer<E, T> setter, T oldValue, T newValue) {
		super(name);
		this.elem = elem;
		this.setter = setter;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

	public ValueAction(E elem, BiConsumer<E, T> setter, T oldValue, T newValue) {
		this.elem = elem;
		this.setter = setter;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

	@Override
	public void undo() {
		setter.accept(elem, oldValue);
	}

	@Override
	public void run() {
		setter.accept(elem, newValue);
	}

}
