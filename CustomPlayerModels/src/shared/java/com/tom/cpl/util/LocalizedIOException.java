package com.tom.cpl.util;

import java.io.IOException;

import com.tom.cpl.text.FormatText;

public class LocalizedIOException extends IOException {
	private static final long serialVersionUID = -6332369839511402034L;
	private FormatText loc;

	public LocalizedIOException(String msg, FormatText loc) {
		super(msg);
		this.loc = loc;
	}

	public LocalizedIOException(String msg, FormatText loc, Throwable thr) {
		super(msg, thr);
		this.loc = loc;
	}

	public FormatText getLoc() {
		return loc;
	}
}