package com.tom.cpl.text;

import java.util.function.BiFunction;
import java.util.function.Function;

public class TextRemapper<C> {
	private BiFunction<String, Object[], C> translate;
	private Function<String, C> string;
	private BiFunction<C, C, C> combine;
	private Function<String, C> keyBind;

	public TextRemapper(BiFunction<String, Object[], C> translate, Function<String, C> string, BiFunction<C, C, C> combine,
			Function<String, C> keyBind) {
		this.translate = translate;
		this.string = string;
		this.combine = combine;
		this.keyBind = keyBind;
	}

	public C translate(String t, Object[] u) {
		return translate.apply(t, u);
	}

	public C string(String t) {
		return string.apply(t);
	}

	public C combine(C t, C u) {
		return combine.apply(t, u);
	}

	public C keyBind(String t) {
		return keyBind.apply(t);
	}

	public boolean hasKeybind() {
		return keyBind != null;
	}
}
