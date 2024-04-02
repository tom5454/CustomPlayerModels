package com.tom.cpm.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Pattern;

import net.minecraft.util.StringTranslate;

import cpw.mods.fml.common.registry.LanguageRegistry;

public class Lang {
	private static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");

	public static void init() {
		LanguageRegistry.instance().addStringLocalization(readLang("en_us"));
	}

	public static Properties readLang(String from) {
		Properties properties = new Properties();
		InputStream is = Lang.class.getResourceAsStream("/assets/cpm/lang/" + from + ".lang");
		if (is == null)return properties;
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"))){
			String s;
			while((s = rd.readLine()) != null) {
				if (!s.isEmpty() && s.charAt(0) != '#') {
					String[] astring = s.split("=", 2);

					if (astring != null && astring.length == 2) {
						String s1 = astring[0];
						String s2 = PATTERN.matcher(astring[1]).replaceAll("%$1s");
						properties.put(s1, s2);
					}
				}
			}
		} catch (IOException e) {}
		return properties;
	}

	public static String format(String key, Object... args) {
		String r = LanguageRegistry.instance().getStringLocalization(key);
		if (r.isEmpty())r = StringTranslate.getInstance().translateKey(key);
		return String.format(r, args);
	}
}
