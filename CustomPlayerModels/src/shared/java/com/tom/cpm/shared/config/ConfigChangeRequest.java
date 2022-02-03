package com.tom.cpm.shared.config;

public class ConfigChangeRequest<K, V> {
	private K key;
	private V oldValue;
	private V newValue;

	public ConfigChangeRequest(K key, V oldValue, V newValue) {
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public K getKey() {
		return key;
	}

	public V getNewValue() {
		return newValue;
	}

	public V getOldValue() {
		return oldValue;
	}
}
