package com.tom.ugwt.client;

import java.util.AbstractList;
import java.util.Collection;

import elemental2.core.JsArray;
import elemental2.promise.Promise;
import jsinterop.base.Js;

public class PromiseArray<R> extends AbstractList<Promise<R>> {
	private JsArray<Promise<R>> promises = new JsArray<>();

	@Override
	public int size() {
		return promises.length;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(Collection<? extends Promise<R>> c) {
		promises.push(c.toArray(new Promise[0]));
		return false;
	}

	@Override
	public Promise<R> get(int index) {
		return promises.getAt(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(Promise<R> e) {
		promises.push(e);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void add(int index, Promise<R> element) {
		promises.splice(index, 0, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		promises = new JsArray<>();
	}

	public Promise<R[]> all() {
		return Promise.all(Js.cast(promises));
	}

	public Promise<R> race() {
		return Promise.race(Js.cast(promises));
	}
}
