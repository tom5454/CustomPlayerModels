package com.tom.ugwt;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UGWTPostProcessor {
	private static final String ARRAY_REGEX = "\\.\\$\\$array_([\\d]+)_\\$\\$";
	private static final String DIRECT_REGEX = "\\$wnd\\.(?:G\\.)?\\$\\$ugwt_m_([\\w\\.]+)_\\$\\$";

	public static String postProcess(String in) {
		{
			Matcher m = Pattern.compile(DIRECT_REGEX).matcher(in);
			while (m.find()) {
				String gr = m.group(1);
				if(gr.length() <= 3) {
					System.out.println("[Warn] Direct access is too short, may be overwritten by minifier");
				}
			}
		}

		in = in.replaceAll(ARRAY_REGEX, "[$1]").replaceAll(DIRECT_REGEX, "$1").replaceAll("\\$wnd\\.goog\\s?=\\s?\\$wnd\\.goog\\s?\\|\\|\\s?\\{\\};", "").
				replaceAll("\\$wnd\\.goog\\.global\\s?=\\s?\\$wnd\\.goog\\.global\\s?\\|\\|\\s?\\$wnd;", "").replace("$wnd.goog.global.", "$wnd.");

		if(System.getProperty("ugwt.useContext", "false").equals("true")) {
			in = "var __ugwt_ctx__ = window.parent;\n" + in;

			String[] contextSensitiveClasses = new String[] {"WebGL\\w*", "Element", "HTML\\w*Element", "File", "Event", "Response", "Canvas\\w*", "Blob", "\\w*Buffer", "Window", "DataTransferItem"};
			in = in.replaceAll("\\$wnd\\.(" + Arrays.stream(contextSensitiveClasses).collect(Collectors.joining("|")) + ")", "__ugwt_ctx__.$1");
		} else {
			in = "var __ugwt_ctx__ = window;\n" + in;
		}
		System.out.println("UGWT Post-Processor finished");

		return in;
	}
}
