package com.tom.cpl.text;

import java.util.function.BiFunction;
import java.util.function.Function;

public class TextRemapper<C> {
	private BiFunction<String, Object[], C> translate;
	private Function<String, C> string;
	private BiFunction<C, C, C> combine;
	private Function<String, C> keyBind;
	private BiFunction<C, TextStyle, C> styling;

	public TextRemapper(BiFunction<String, Object[], C> translate, Function<String, C> string, BiFunction<C, C, C> combine,
			Function<String, C> keyBind, BiFunction<C, TextStyle, C> styling) {
		this.translate = translate;
		this.string = string;
		this.combine = combine;
		this.keyBind = keyBind;
		this.styling = styling;
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

	public static TextRemapper<String> stringMapper(BiFunction<String, Object[], String> translate) {
		return new TextRemapper<>(translate, Function.identity(), (a, b) -> a + b, null, TextRemapper::formattingCodeStyling);
	}

	private static String formattingCodeStyling(String in, TextStyle style) {
		StringBuilder sb = new StringBuilder("\u00A7r");
		if(style.bold)sb.append("\u00A7l");
		if(style.italic)sb.append("\u00A7o");
		if(style.underline)sb.append("\u00A7n");
		if(style.strikethrough)sb.append("\u00A7m");
		sb.append(in);
		return sb.toString();
	}

	public C styling(C to, TextStyle style) {
		if(styling != null)return styling.apply(to, style);
		else return to;
	}
}
