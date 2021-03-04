package com.tom.cpm.shared.util;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlatListView<L, E> extends AbstractList<E> {
	private List<L> list;
	private Function<L, Stream<E>> flatMap;
	private List<E> evalList;

	public FlatListView(List<L> list, Function<L, Stream<E>> flatMap) {
		this.list = list;
		this.flatMap = flatMap;
	}

	@Override
	public E get(int index) {
		if(evalList == null)size();
		return evalList.get(index);
	}

	@Override
	public int size() {
		evalList = list.stream().flatMap(flatMap).collect(Collectors.toList());
		return evalList.size();
	}

}
