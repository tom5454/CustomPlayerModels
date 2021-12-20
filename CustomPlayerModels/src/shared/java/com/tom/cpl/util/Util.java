package com.tom.cpl.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;

public class Util {

	public static <T> List<T> listFromTree(Consumer<Consumer<T>> tree) {
		List<T> lst = new ArrayList<>();
		tree.accept(lst::add);
		return lst;
	}

	public static Map<String, Object> deepCopy(Map<String, Object> data) {
		Map<String, Object> map = new HashMap<>();
		for(Entry<String, Object> e : data.entrySet()) {
			map.put(e.getKey(), deepCopy0(e.getValue()));
		}
		return map;
	}

	public static List<Object> deepCopy(List<Object> data) {
		List<Object> list = new ArrayList<>();
		for (Object object : data) {
			list.add(deepCopy0(object));
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private static Object deepCopy0(Object data) {
		if(data instanceof Map)return deepCopy((Map<String, Object>) data);
		else if(data instanceof List)return deepCopy((List<Object>) data);
		else return data;
	}

	public static UUID uuidFromString(final String input) {
		if(input.indexOf('-') != -1)
			return UUID.fromString(input);
		else
			return UUID.fromString(input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
	}

	public static void closeQuietly(final Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (final IOException ioe) {
			// ignore
		}
	}
}
