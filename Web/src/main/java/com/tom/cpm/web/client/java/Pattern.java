package com.tom.cpm.web.client.java;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class Pattern {
	private final RegExp reg;
	public static final String CASE_INSENSITIVE = "i";

	public Pattern(RegExp reg) {
		this.reg = reg;
	}

	public static Pattern compile(String regex) {
		return new Pattern(RegExp.compile(regex));
	}

	public static Pattern compile(String regex, String flags) {
		return new Pattern(RegExp.compile(regex));
	}

	public static String quote(String s) {
		int slashEIndex = s.indexOf("\\E");
		if (slashEIndex == -1)
			return "\\Q" + s + "\\E";

		StringBuilder sb = new StringBuilder(s.length() * 2);
		sb.append("\\Q");
		slashEIndex = 0;
		int current = 0;
		while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
			sb.append(s.substring(current, slashEIndex));
			current = slashEIndex + 2;
			sb.append("\\E\\\\E\\Q");
		}
		sb.append(s.substring(current, s.length()));
		sb.append("\\E");
		return sb.toString();
	}

	public Matcher matcher(String v) {
		return new Matcher(reg.exec(v));
	}

	public static class Matcher {
		private final MatchResult res;

		public Matcher(MatchResult res) {
			this.res = res;
		}

		public boolean find() {
			return res != null && res.getGroupCount() > 0;
		}

		public String group(int index) {
			return res.getGroup(index);
		}

		public boolean matches() {
			return res != null;
		}
	}
}
