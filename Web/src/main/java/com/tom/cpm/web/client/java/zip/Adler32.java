package com.tom.cpm.web.client.java.zip;

public class Adler32 implements Checksum {
	private com.jcraft.jzlib.Adler32 a32 = new com.jcraft.jzlib.Adler32();

	@Override
	public void update(int b) {
		a32.update(new byte[] {(byte) b}, 0, 1);
	}

	@Override
	public void update(byte[] b, int off, int len) {
		a32.update(b, off, len);
	}

	@Override
	public long getValue() {
		return a32.getValue();
	}

	@Override
	public void reset() {
		a32.reset();
	}
}
