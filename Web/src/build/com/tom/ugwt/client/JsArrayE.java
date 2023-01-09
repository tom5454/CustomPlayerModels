package com.tom.ugwt.client;

import java.util.function.Consumer;

import elemental2.core.JsArray;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

@JsType(isNative = true, name = "Array", namespace = JsPackage.GLOBAL)
public class JsArrayE<E> extends JsArray<E> {

	@SuppressWarnings("unchecked")
	public JsArrayE(E... items) {
		super(items);
	}

	@JsOverlay
	public final void forEach(Consumer<? super E> f) {
		forEach((e, __, ___) -> {
			f.accept(e);
			return null;
		});
	}

	@Override
	public native JsArrayE<E> slice();

	public native void forEachReverse(ForEachCallbackFn<E> cb);

	@JsOverlay
	public final void forEachReverse(Consumer<E> f) {
		forEachReverse((e, __, ___) -> {
			f.accept(e);
			return null;
		});
	}

	@JsOverlay
	public final E[] array() {
		return Js.uncheckedCast(this);
	}
}
