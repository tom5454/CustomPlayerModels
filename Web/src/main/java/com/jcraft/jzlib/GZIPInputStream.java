package com.jcraft.jzlib;

import java.io.IOException;
import java.io.InputStream;

public class GZIPInputStream extends InflaterInputStream {

	public GZIPInputStream(InputStream in) throws IOException {
		this(in, DEFAULT_BUFSIZE, true);
	}

	public GZIPInputStream(InputStream in, int size, boolean close_in) throws IOException {
		this(in, new Inflater(15 + 16), size, close_in);
		myinflater = true;
	}

	public GZIPInputStream(InputStream in, Inflater inflater, int size, boolean close_in) throws IOException {
		super(in, inflater, size, close_in);
	}

	public long getModifiedtime() {
		return inflater.istate.getGZIPHeader().getModifiedTime();
	}

	public int getOS() {
		return inflater.istate.getGZIPHeader().getOS();
	}

	public String getName() {
		return inflater.istate.getGZIPHeader().getName();
	}

	public String getComment() {
		return inflater.istate.getGZIPHeader().getComment();
	}

	public long getCRC() throws GZIPException {
		if (inflater.istate.mode != 12 /* DONE */) throw new GZIPException("checksum is not calculated yet.");
		return inflater.istate.getGZIPHeader().getCRC();
	}

	public void readHeader() throws IOException {

		byte[] empty = "".getBytes();
		inflater.setOutput(empty, 0, 0);
		inflater.setInput(empty, 0, 0, false);

		byte[] b = new byte[10];

		int n = fill(b);
		if (n != 10) {
			if (n > 0) {
				inflater.setInput(b, 0, n, false);
				// inflater.next_in_index = n;
				inflater.next_in_index = 0;
				inflater.avail_in = n;
			}
			throw new IOException("no input");
		}

		inflater.setInput(b, 0, n, false);

		byte[] b1 = new byte[1];
		do {
			if (inflater.avail_in <= 0) {
				int i = in.read(b1);
				if (i <= 0) throw new IOException("no input");
				inflater.setInput(b1, 0, 1, true);
			}

			int err = inflater.inflate(JZlib.Z_NO_FLUSH);

			if (err != 0/* Z_OK */) {
				int len = 2048 - inflater.next_in.length;
				if (len > 0) {
					byte[] tmp = new byte[len];
					n = fill(tmp);
					if (n > 0) {
						inflater.avail_in += inflater.next_in_index;
						inflater.next_in_index = 0;
						inflater.setInput(tmp, 0, n, true);
					}
				}
				// inflater.next_in_index = inflater.next_in.length;
				inflater.avail_in += inflater.next_in_index;
				inflater.next_in_index = 0;
				throw new IOException(inflater.msg);
			}
		} while (inflater.istate.inParsingHeader());
	}

	private int fill(byte[] buf) {
		int len = buf.length;
		int n = 0;
		do {
			int i = -1;
			try {
				i = in.read(buf, n, buf.length - n);
			} catch (IOException e) {
			}
			if (i == -1) {
				break;
			}
			n += i;
		} while (n < len);
		return n;
	}
}
