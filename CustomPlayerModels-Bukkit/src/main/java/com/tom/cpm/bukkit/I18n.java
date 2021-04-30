package com.tom.cpm.bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class I18n {
	/** Splits on "=" */
	private static final Splitter SPLITTER = Splitter.on('=').limit(2);
	private static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
	private Map<String, String> properties = Maps.<String, String>newHashMap();
	protected I18n() {
	}
	public String format(String translateKey, Object... parameters){
		String s = translateKeyPrivate(translateKey);

		try {
			return String.format(s, parameters);
		} catch (IllegalFormatException var5) {
			return "Format error: " + s;
		}
	}

	private String translateKeyPrivate(String translateKey) {
		String s = properties.get(translateKey);
		return s == null ? translateKey : s;
	}

	public static I18n loadLocaleData(InputStream is) throws IOException {
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))){
			I18n ret = new I18n();
			rd.lines().forEach(s -> {
				if (!s.isEmpty() && s.charAt(0) != '#') {
					String[] astring = Iterables.toArray(SPLITTER.split(s), String.class);

					if (astring != null && astring.length == 2) {
						String s1 = astring[0];
						String s2 = PATTERN.matcher(astring[1]).replaceAll("%$1s");
						ret.properties.put(s1, s2);
					}
				}
			});
			return ret;
		}
	}
}
