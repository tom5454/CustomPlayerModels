package com.tom.cpm.web.client.java.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.tom.cpm.web.client.FS;

public class FileInputStream extends InputStream {
	private ByteArrayInputStream bais;

	public FileInputStream(File file) throws FileNotFoundException {
		bais = new ByteArrayInputStream(Base64.getDecoder().decode(FS.getContent(file.getAbsolutePath())));
	}

	@Override
	public int read(byte[] b) throws IOException {
		if(bais == null)throw new IOException("File closed");
		return bais.read(b);
	}

	@Override
	public int read() throws IOException {
		if(bais == null)throw new IOException("File closed");
		return bais.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if(bais == null)throw new IOException("File closed");
		return bais.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		if(bais == null)throw new IOException("File closed");
		return bais.skip(n);
	}

	@Override
	public int available() throws IOException {
		if(bais == null)throw new IOException("File closed");
		return bais.available();
	}

	@Override
	public boolean markSupported() {
		if(bais != null)
			return bais.markSupported();
		return false;
	}

	@Override
	public void mark(int readAheadLimit) {
		if(bais != null)
			bais.mark(readAheadLimit);
	}

	@Override
	public void reset() throws IOException {
		if(bais == null)throw new IOException("File closed");
		bais.reset();
	}

	@Override
	public void close() throws IOException {
		bais = null;
	}
}
