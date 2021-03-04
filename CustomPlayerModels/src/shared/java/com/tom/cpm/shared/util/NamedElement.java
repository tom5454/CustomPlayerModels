package com.tom.cpm.shared.util;

import java.util.function.Function;

public class NamedElement<T> {
	private T elem;
	private Function<T, String> tostring;
	public NamedElement(T elem, Function<T, String> tostring) {
		this.elem = elem;
		this.tostring = tostring;
	}

	public T getElem() {
		return elem;
	}

	@Override
	public String toString() {
		return tostring.apply(elem);
	}
}
