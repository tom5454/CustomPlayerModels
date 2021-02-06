package com.tom.cpm.shared.util;

import java.util.Map.Entry;

public class Pair<K, V> implements Entry<K, V> {
	protected K key;
	protected V value;

	@Override
	public K getKey() {
		return key;
	}
	@Override
	public V getValue() {
		return value;
	}
	public void setKey(K key) {
		this.key = key;
	}
	@Override
	public V setValue(V value) {
		V old = this.value;
		this.value = value;
		return old;
	}
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}
	public static <K, V> Pair<K, V> of(K key, V value){
		return new Pair<>(key, value);
	}
	public static <K, V> UnmodifiablePair<K, V> ofUnmodifiable(K key, V value){
		return new UnmodifiablePair<>(key, value);
	}
	public UnmodifiablePair<K, V> toUnmodifiable(){
		return new UnmodifiablePair<>(key, value);
	}
	public static class UnmodifiablePair<K, V> extends Pair<K, V>{

		public UnmodifiablePair(K key, V value) {
			super(key, value);
		}
		@Override
		public void setKey(K key) {
			throw new UnsupportedOperationException("Cannot set the key of an UnmodifiablePair");
		}
		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException("Cannot set the value of an UnmodifiablePair");
		}
		@Override
		public UnmodifiablePair<K, V> toUnmodifiable() {
			return this;
		}
	}
	@Override
	public String toString() {
		return key + "=" + value;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	public Pair<K, V> copy(){
		return new Pair<>(key, value);
	}
}
