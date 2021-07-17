package com.tom.cpm.shared.definition;

import java.util.Map;

import com.tom.cpm.shared.editor.actions.Action;

public abstract class MapAction<K, V> extends Action {
	private Map<K, V> map;
	private K key;
	private V value;

	public MapAction(Map<K, V> map, K key, V value) {
		this.map = map;
		this.key = key;
		this.value = value;
	}
	public static <K, V> MapAction<K, V> add(Map<K, V> map, K key, V value) {
		return new Add<>(map, key, value);
	}

	public static <K, V> MapAction<K, V> remove(Map<K, V> map, K key, V value) {
		return new Remove<>(map, key, value);
	}

	protected void add() {
		map.put(key, value);
	}

	protected void remove() {
		map.remove(key);
	}

	private static class Add<K, V> extends MapAction<K, V> {

		public Add(Map<K, V> map, K key, V value) {
			super(map, key, value);
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

	private static class Remove<K, V> extends MapAction<K, V> {

		public Remove(Map<K, V> map, K key, V value) {
			super(map, key, value);
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
