package com.jcraft.jzlib;

import java.io.IOException;
import java.io.OutputStream;

public class GZIPOutputStream extends DeflaterOutputStream {

	public GZIPOutputStream(OutputStream out) throws IOException {
		this(out, DEFAULT_BUFSIZE);
	}

	public GZIPOutputStream(OutputStream out, int size) throws IOException {
		this(out, size, true);
	}

	public GZIPOutputStream(OutputStream out, int size, boolean close_out) throws IOException {
		this(out, new Deflater(JZlib.Z_DEFAULT_COMPRESSION, 15 + 16), size, close_out);
		mydeflater = true;
	}

	public GZIPOutputStream(OutputStream out, Deflater deflater, int size, boolean close_out) throws IOException {
		super(out, deflater, size, close_out);
	}

	private void check() throws GZIPException {
		if (deflater.dstate.status != 42 /* INIT_STATUS */) throw new GZIPException("header is already written.");
	}

	public void setModifiedTime(long mtime) throws GZIPException {
		check();
		deflater.dstate.getGZIPHeader().setModifiedTime(mtime);
	}

	public void setOS(int os) throws GZIPException {
		check();
		deflater.dstate.getGZIPHeader().setOS(os);
	}

	public void setName(String name) throws GZIPException {
		check();
		deflater.dstate.getGZIPHeader().setName(name);
	}

	public void setComment(String comment) throws GZIPException {
		check();
		deflater.dstate.getGZIPHeader().setComment(comment);
	}

	public long getCRC() throws GZIPException {
		if (deflater.dstate.status != 666 /* FINISH_STATE */)
			throw new GZIPException("checksum is not calculated yet.");
		return deflater.dstate.getGZIPHeader().getCRC();
	}
}
