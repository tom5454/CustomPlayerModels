package com.tom.cpm.web.client.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import elemental2.core.Global;

public class Properties {
	private Map<String, String> map = new HashMap<>();

	public Set<String> stringPropertyNames() {
		return map.keySet();
	}

	public String getProperty(String key) {
		return map.get(key);
	}

	public void load(InputStream is) throws IOException {
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
			map = rd.lines().filter(s -> !s.startsWith("#")).map(s -> s.split("=")).collect(Collectors.toMap(s -> s[0], s -> unescape(s[1])));
		}
	}

	private static String unescape(String string) {
		return (String) Global.JSON.parse("\"" + string + "\"");
	}
}
