package com.tom.cpl.util;

import java.io.PrintStream;

import com.tom.ugwt.client.ExceptionUtil;

public class StringBuilderStream extends PrintStream {
	private StringBuilder bb;
	private String sep;

	public StringBuilderStream(StringBuilder bb, String sep) {
		super(System.err);
		this.bb = bb;
		this.sep = sep;
	}

	@Override
	public void println(Object x) {
		bb.append(x);
		bb.append(sep);
	}

	@Override
	public void println(String x) {
		bb.append(x);
		bb.append(sep);
	}

	public static void stacktraceToString(Throwable t, StringBuilder sb, String sep) {
		sb.append(ExceptionUtil.getStackTrace(t, true).replace("\t", "").replace("\n", sep));
	}
}
