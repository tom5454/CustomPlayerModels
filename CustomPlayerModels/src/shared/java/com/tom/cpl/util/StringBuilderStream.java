package com.tom.cpl.util;

import java.io.PrintStream;

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
}
