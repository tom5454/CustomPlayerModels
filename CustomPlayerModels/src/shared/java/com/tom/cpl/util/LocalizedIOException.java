package com.tom.cpl.util;

import java.io.IOException;

import com.tom.cpl.text.IText;

public class LocalizedIOException extends IOException implements LocalizedException {
	private static final long serialVersionUID = -6332369839511402034L;
	private IText loc;

	public LocalizedIOException(String msg, IText loc) {
		super(msg);
		this.loc = loc;
	}

	public LocalizedIOException(String msg, IText loc, Throwable thr) {
		super(msg, thr);
		this.loc = loc;
	}

	@Override
	public IText getLocalizedText() {
		return loc;
	}
}