package com.tom.cpl.util;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

public class CombinedListView<E> extends AbstractList<E> {
	private List<List<E>> lists;

	@SafeVarargs
	public CombinedListView(List<E>... lists) {
		this.lists = Arrays.asList(lists);
	}

	@Override
	public E get(int index) {
		int ind = 0;
		List<E> lst;
		while((lst = lists.get(ind++)).size() <= index) {
			index -= lst.size();
		}
		return lst.get(index);
	}

	@Override
	public int size() {
		return lists.stream().mapToInt(List::size).sum();
	}

}
