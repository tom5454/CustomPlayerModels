package com.jcraft.jzlib;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * ZOutputStream
 *
 * @deprecated use DeflaterOutputStream or InflaterInputStream
 */
@Deprecated
public class ZOutputStream extends FilterOutputStream {

	protected int bufsize = 512;
	protected int flush = JZlib.Z_NO_FLUSH;
	protected byte[] buf = new byte[bufsize];
	protected boolean compress;

	protected OutputStream out;
	private boolean end = false;

	private DeflaterOutputStream dos;
	private Inflater inflater;

	public ZOutputStream(OutputStream out) throws IOException {
		super(out);
		this.out = out;
		inflater = new Inflater();
		inflater.init();
		compress = false;
	}

	public ZOutputStream(OutputStream out, int level) throws IOException {
		this(out, level, false);
	}

	public ZOutputStream(OutputStream out, int level, boolean nowrap) throws IOException {
		super(out);
		this.out = out;
		Deflater deflater = new Deflater(level, nowrap);
		dos = new DeflaterOutputStream(out, deflater);
		compress = true;
	}

	private byte[] buf1 = new byte[1];

	public void write(int b) throws IOException {
		buf1[0] = (byte) b;
		write(buf1, 0, 1);
	}

	public void write(byte b[], int off, int len) throws IOException {
		if (len == 0) return;
		if (compress) {
			dos.write(b, off, len);
		} else {
			inflater.setInput(b, off, len, true);
			int err = JZlib.Z_OK;
			while (inflater.avail_in > 0) {
				inflater.setOutput(buf, 0, buf.length);
				err = inflater.inflate(flush);
				if (inflater.next_out_index > 0) out.write(buf, 0, inflater.next_out_index);
				if (err != JZlib.Z_OK) break;
			}
			if (err != JZlib.Z_OK) throw new ZStreamException("inflating: " + inflater.msg);
			return;
		}
	}

	public int getFlushMode() {
		return flush;
	}

	public void setFlushMode(int flush) {
		this.flush = flush;
	}

	public void finish() throws IOException {
		int err;
		if (compress) {
			int tmp = flush;
			int flush = JZlib.Z_FINISH;
			try {
				write("".getBytes(), 0, 0);
			} finally {
				flush = tmp;
			}
		} else {
			dos.finish();
		}
		flush();
	}

	public synchronized void end() {
		if (end) return;
		if (compress) {
			try {
				dos.finish();
			} catch (Exception e) {
			}
		} else {
			inflater.end();
		}
		end = true;
	}

	public void close() throws IOException {
		try {
			try {
				finish();
			} catch (IOException ignored) {
			}
		} finally {
			end();
			out.close();
			out = null;
		}
	}

	public long getTotalIn() {
		if (compress) return dos.getTotalIn();
		else return inflater.total_in;
	}

	public long getTotalOut() {
		if (compress) return dos.getTotalOut();
		else return inflater.total_out;
	}

	public void flush() throws IOException {
		out.flush();
	}

}
