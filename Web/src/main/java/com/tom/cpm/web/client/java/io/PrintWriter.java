package com.tom.cpm.web.client.java.io;

import java.io.IOException;
import java.io.Writer;

public class PrintWriter extends Writer {
	private static final String lineSeparator = "\n";
	protected Writer out;

	public PrintWriter(Writer out) {
		this.out = out;
	}

	@Override
	public void write(char buf[], int off, int len) {
		try {
			ensureOpen();
			out.write(buf, off, len);
		} catch (IOException x) {
		}
	}

	@Override
	public void write(char buf[]) {
		write(buf, 0, buf.length);
	}

	@Override
	public void write(String s, int off, int len) {
		try {
			ensureOpen();
			out.write(s, off, len);
		} catch (IOException x) {
		}
	}

	@Override
	public void write(String s) {
		write(s, 0, s.length());
	}

	private void newLine() {
		try {
			ensureOpen();
			out.write(lineSeparator);
		} catch (IOException x) {
		}
	}

	@Override
	public void flush() {
		try {
			ensureOpen();
			out.flush();
		} catch (IOException x) {
		}
	}

	@Override
	public void close() {
		try {
			if (out == null)
				return;
			out.close();
			out = null;
		} catch (IOException x) {
		}
	}

	private void ensureOpen() throws IOException {
		if (out == null)
			throw new IOException("Stream closed");
	}

	public void print(String s) {
		if (s == null) {
			s = "null";
		}
		write(s);
	}

	public void print(Object obj) {
		write(String.valueOf(obj));
	}

	public void println() {
		newLine();
	}

	public void println(String x) {
		print(x);
		println();
	}

	public void println(Object x) {
		String s = String.valueOf(x);
		print(s);
		println();
	}
}
