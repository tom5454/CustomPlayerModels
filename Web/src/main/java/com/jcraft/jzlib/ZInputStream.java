package com.jcraft.jzlib;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ZInputStream
 *
 * @deprecated use DeflaterOutputStream or InflaterInputStream
 */
@Deprecated
public class ZInputStream extends FilterInputStream {

	protected int flush = JZlib.Z_NO_FLUSH;
	protected boolean compress;
	protected InputStream in = null;

	protected Deflater deflater;
	protected InflaterInputStream iis;

	public ZInputStream(InputStream in) throws IOException {
		this(in, false);
	}

	public ZInputStream(InputStream in, boolean nowrap) throws IOException {
		super(in);
		iis = new InflaterInputStream(in, nowrap);
		compress = false;
	}

	public ZInputStream(InputStream in, int level) throws IOException {
		super(in);
		this.in = in;
		deflater = new Deflater();
		deflater.init(level);
		compress = true;
	}

	private byte[] buf1 = new byte[1];

	public int read() throws IOException {
		if (read(buf1, 0, 1) == -1) return -1;
		return (buf1[0] & 0xFF);
	}

	private byte[] buf = new byte[512];

	public int read(byte[] b, int off, int len) throws IOException {
		if (compress) {
			deflater.setOutput(b, off, len);
			while (true) {
				int datalen = in.read(buf, 0, buf.length);
				if (datalen == -1) return -1;
				deflater.setInput(buf, 0, datalen, true);
				int err = deflater.deflate(flush);
				if (deflater.next_out_index > 0) return deflater.next_out_index;
				if (err == JZlib.Z_STREAM_END) return 0;
				if (err == JZlib.Z_STREAM_ERROR || err == JZlib.Z_DATA_ERROR) {
					throw new ZStreamException("deflating: " + deflater.msg);
				}
			}
		} else {
			return iis.read(b, off, len);
		}
	}

	public long skip(long n) throws IOException {
		int len = 512;
		if (n < len) len = (int) n;
		byte[] tmp = new byte[len];
		return ((long) read(tmp));
	}

	public int getFlushMode() {
		return flush;
	}

	public void setFlushMode(int flush) {
		this.flush = flush;
	}

	public long getTotalIn() {
		if (compress) return deflater.total_in;
		else return iis.getTotalIn();
	}

	public long getTotalOut() {
		if (compress) return deflater.total_out;
		else return iis.getTotalOut();
	}

	public void close() throws IOException {
		if (compress) deflater.end();
		else iis.close();
	}
}
