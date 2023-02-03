package com.tom.cpm.web.gwt;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ClasspathFix {

	public static String getFixedClasspath() {
		String[] cp = System.getProperty("java.class.path").split(";");
		Map<String, Integer> sortMap = new HashMap<>();
		for (int i = 0; i < cp.length; i++) {
			if(cp[i].endsWith(".jar"))
				sortMap.put(cp[i], i + 100);
			else
				sortMap.put(cp[i], i);
		}
		return Arrays.stream(cp).sorted(Comparator.comparingInt(sortMap::get)).collect(Collectors.joining(";"));
	}
}
