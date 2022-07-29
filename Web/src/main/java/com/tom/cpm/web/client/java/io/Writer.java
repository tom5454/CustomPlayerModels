package com.tom.cpm.web.client.java.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public class Writer implements Appendable, Closeable, Flushable {

	@Override
	public void flush() throws IOException {

	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public Appendable append(CharSequence csq) throws IOException {
		return null;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		return null;
	}

	@Override
	public Appendable append(char c) throws IOException {
		return null;
	}

}
