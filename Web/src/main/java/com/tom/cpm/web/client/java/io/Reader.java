package com.tom.cpm.web.client.java.io;

import java.io.Closeable;
import java.io.IOException;

public abstract class Reader implements Closeable {
	public int read() throws IOException {
		char chr[] = new char[1];
		return (read(chr) == -1) ? -1 : chr[0];
	}

	public int read(char[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}

	public abstract int read(char[] cbuf, int off, int len) throws IOException;

	public boolean markSupported() {
		return false;
	}

	public void mark(int readAheadLimit) throws IOException {
		throw new IOException("Not supported");
	}

	public boolean ready() throws IOException {
		return false;
	}

	public void reset() throws IOException {
		throw new IOException("Not supported");
	}

	public long skip(long n) throws IOException {
		long remaining = n;
		int bufferSize = Math.min((int) n, 1024);
		char[] skipBuffer = new char[bufferSize];
		while (remaining > 0) {
			long numRead = read(skipBuffer, 0, (int) remaining);
			if (numRead < 0) {
				break;
			}
			remaining -= numRead;
		}
		return n - remaining;
	}

}
