package com.tom.cpm.web.client.java.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class OutputStreamWriter extends Writer {
	private StringBuilder b = new StringBuilder();
	private OutputStream os;

	public OutputStreamWriter(OutputStream os) {
		this.os = os;
	}

	@Override
	public Writer append(char c) throws IOException {
		b.append(c);
		return this;
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		b.append(csq);
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		b.append(csq, start, end);
		return this;
	}

	@Override
	public void close() throws IOException {
		os.write(b.toString().getBytes(StandardCharsets.UTF_8));
		os.close();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		b.append(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException {
	}
}
