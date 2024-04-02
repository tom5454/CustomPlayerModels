package com.tom.cpm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CPMVersion {
	private static final String version;

	static {
		String v = "???";
		InputStream is = CPMVersion.class.getResourceAsStream("/cpm.version");
		if (is == null)v = "";
		else
			try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
				v = rd.readLine();
			} catch (IOException e) {
			}
		version = v;
	}

	public static String getVersion() {
		return version;
	}
}
