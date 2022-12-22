package com.tom.cpm.web.client.java.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

import com.tom.cpm.web.client.FS;

public class FileOutputStream extends OutputStream {
	private ByteArrayOutputStream baos;
	private File file;

	public FileOutputStream(File file) throws IOException {
		this.file = file;
		baos = new ByteArrayOutputStream();
	}

	@Override
	public void write(byte[] b) throws IOException {
		if(baos == null)throw new IOException("File closed");
		baos.write(b);
	}

	@Override
	public void write(int b) throws IOException {
		if(baos == null)throw new IOException("File closed");
		baos.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if(baos == null)throw new IOException("File closed");
		baos.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		if(baos == null)throw new IOException("File closed");
		FS.setContent(file.getAbsolutePath(), Base64.getEncoder().encodeToString(baos.toByteArray()));
	}

	@Override
	public void close() throws IOException {
		if(baos != null)flush();
		baos = null;
	}
}
