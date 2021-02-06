package com.tom.cpm.shared.io;

import java.io.IOException;
import java.io.OutputStream;

public class ChecksumOutputStream extends OutputStream {
	private OutputStream os;
	private short sum;

	public ChecksumOutputStream(OutputStream os) {
		this.os = os;
	}

	@Override
	public void write(int b) throws IOException {
		os.write(b);
		sum += (b & 0xff);
	}

	@Override
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public void close() throws IOException {
		try {
			os.write((sum >>> 8) & 0xFF);
			os.write((sum >>> 0) & 0xFF);
		} finally {
			os.close();
		}
	}
}
