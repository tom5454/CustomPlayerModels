package com.tom.cpl.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.tom.cpl.text.I18n;

public class EmbeddedLocalization {
	public static final String LOCALE = "meta.cpm.embedLocale";
	public static boolean validateEmbeds = true;
	private static List<EmbeddedLocalization> allEntries = new ArrayList<>();
	private final String key;
	private Map<String, String> translations = new HashMap<>();
	private String fallback;

	protected EmbeddedLocalization(String key) {
		this.key = key;
		allEntries.add(this);
	}

	public String getTextSafe(I18n gui, Object... args) {
		String locale = gui.i18nFormat(LOCALE);
		String val = translations.getOrDefault(locale, fallback);
		return String.format(val, args);
	}

	public String getTextNormal(I18n gui, Object... args) {
		return gui.i18nFormat(key, args);
	}

	public void setFallback(String fallback) {
		this.fallback = decode(fallback);
	}

	public void addLocale(String loc, String text) {
		translations.put(loc, decode(text));
	}

	private String decode(String text) {
		return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
	}

	public static void validateEmbeds() {
		if(validateEmbeds) {
			if(allEntries.stream().anyMatch(e -> e.fallback == null))
				throw new RuntimeException("Embedded translations are corrupted");
			allEntries = null;
		}
	}

	public String getKey() {
		return key;
	}

	public static void forEachEntry(Consumer<? super EmbeddedLocalization> action) {
		allEntries.forEach(action);
	}
}
