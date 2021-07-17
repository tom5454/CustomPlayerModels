package com.tom.cpm.shared.definition;

import java.util.List;

import com.tom.cpm.shared.editor.actions.Action;

public abstract class ListAction<T> extends Action {
	private T value;
	private List<T> list;
	private int index = -1;

	public ListAction(T value, List<T> list) {
		this.value = value;
		this.list = list;
	}

	public static <T> ListAction<T> add(T value, List<T> list) {
		return new Add<>(value, list);
	}

	public static <T> ListAction<T> remove(T value, List<T> list) {
		return new Remove<>(value, list);
	}

	protected void add() {
		if(index != -1 && index < list.size())list.add(index, value);
		else list.add(value);
	}

	protected void remove() {
		index = list.indexOf(value);
		list.remove(value);
	}

	private static class Add<T> extends ListAction<T> {

		public Add(T value, List<T> list) {
			super(value, list);
		}

		@Override
		public void undo() {
			remove();
		}

		@Override
		public void run() {
			add();
		}
	}

	private static class Remove<T> extends ListAction<T> {

		public Remove(T value, List<T> list) {
			super(value, list);
		}

		@Override
		public void undo() {
			add();
		}

		@Override
		public void run() {
			remove();
		}
	}
}
