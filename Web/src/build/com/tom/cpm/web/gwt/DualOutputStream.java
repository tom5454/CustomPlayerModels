package com.tom.cpm.web.gwt;

import java.io.IOException;
import java.io.OutputStream;

public class DualOutputStream extends OutputStream {
	private OutputStream stream1, stream2;

	public DualOutputStream(OutputStream stream1, OutputStream stream2) {
		this.stream1 = stream1;
		this.stream2 = stream2;
	}

	@Override
	public void write(int b) throws IOException {
		stream1.write(b);
		stream2.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		stream1.write(b);
		stream2.write(b);
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		stream1.write(b, off, len);
		stream2.write(b, off, len);
	}
	@Override
	public void flush() throws IOException {
		stream1.flush();
		stream2.flush();
	}
	@Override
	public void close() throws IOException {
		stream1.close();
		stream2.close();
	}
}