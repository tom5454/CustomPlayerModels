package com.tom.cpm.web.client.render;

import java.util.stream.Collectors;

import com.tom.cpm.web.client.resources.Resources;

import elemental2.promise.Promise;

public class FontLoader {
	public static final Promise<Object> loaded;
	public static String FONTS;
	static {
		loaded = Resources.loaded.then(__ -> {
			FONTS = Resources.listResources("assets", r -> r.startsWith("font/") && r.endsWith(".ttf")).
					map(r ->
					"@font-face {\n"
					+ "font-family: " + r.substring(r.lastIndexOf('/') + 1, r.length() - 4) + ";\n"
					+ "src: url(data:font/truetype;base64," + Resources.getResource(r) + ");\n"
					+ "}\n"
							).collect(Collectors.joining("\n"));
			return null;
		});
	}
}
