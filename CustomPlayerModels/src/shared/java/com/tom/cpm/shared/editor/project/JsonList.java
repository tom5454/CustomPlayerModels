package com.tom.cpm.shared.editor.project;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.tom.cpl.util.ThrowingConsumer;

public interface JsonList {

	int size();
	Object get(int i);
	JsonMap getMap(int i);

	default <E extends Throwable> void forEachMap(ThrowingConsumer<JsonMap, E> c) throws E {
		for(int i = 0;i<size();i++) {
			c.accept(getMap(i));
		}
	}

	@SuppressWarnings("unchecked")
	default <T> void forEach(Consumer<T> c) {
		for(int i = 0;i<size();i++) {
			c.accept((T) get(i));
		}
	}

	@SuppressWarnings("unchecked")
	default <T> Stream<T> stream() {
		return (Stream<T>) IntStream.range(0, size()).mapToObj(this::get);
	}

	void add(Object data);

	default JsonMap addMap() {
		add(new HashMap<>());
		return getMap(size() - 1);
	}
}
