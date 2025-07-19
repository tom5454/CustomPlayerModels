package com.tom.cpm.web.client.java.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.java.Java;

import elemental2.promise.Promise;

public class FileInputStream extends InputStream {
	private ByteArrayInputStream bais;
	private String b64;

	@Deprecated
	public FileInputStream(File file) throws FileNotFoundException {
		this(FS.getContentSync(file.getAbsolutePath()));
	}

	private FileInputStream(String b64) {
		this.b64 = b64;
		bais = new ByteArrayInputStream(Base64.getDecoder().decode(b64));
	}

	public static CompletableFuture<FileInputStream> openFile(File file) {
		CompletableFuture<FileInputStream> cf = new CompletableFuture<>();
		Promise<FileInputStream> p = FS.getContent(file.getAbsolutePath()).then(d -> Promise.resolve(new FileInputStream(d)));
		Java.promiseToCf(p, cf);
		return cf;
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
		b64 = null;
	}

	public String getB64() {
		return b64;
	}
}
