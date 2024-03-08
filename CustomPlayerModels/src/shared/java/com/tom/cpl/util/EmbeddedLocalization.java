package com.tom.cpl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.tom.cpl.text.I18n;

public class EmbeddedLocalization {
	public static final String LOCALE = "meta.cpm.embedLocale";
	public static final String LOCALE_ID = "meta.cpm.localeID";
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
		this.fallback = fallback;
	}

	public void addLocale(String loc, String text) {
		translations.put(loc, text);
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

	public static String getLocalizedWikiPage(I18n gui, String path) {
		String loc = gui.i18nFormat(LOCALE_ID);
		if (loc.equals("en_us") || loc.equals(LOCALE_ID))
			return path;
		else {
			String[] sp = loc.split("_");
			return path + "-" + sp[0] + "-" + sp[1].toUpperCase(Locale.ROOT);
		}
	}
}
