package com.tom.cpl.util;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

public class ListView<T, E> extends AbstractList<E> {
	private List<T> parent;
	private Function<T, E> viewCreator;
	public ListView(List<T> parent, Function<T, E> viewCreator) {
		this.parent = parent;
		this.viewCreator = viewCreator;
	}

	@Override
	public E get(int index) {
		return viewCreator.apply(parent.get(index));
	}

	@Override
	public int size() {
		return parent.size();
	}

}
