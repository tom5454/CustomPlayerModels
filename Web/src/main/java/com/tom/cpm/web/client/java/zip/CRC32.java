package com.tom.cpm.web.client.java.zip;

public class CRC32 implements Checksum {
	private com.jcraft.jzlib.CRC32 crc = new com.jcraft.jzlib.CRC32();

	@Override
	public void update(byte[] buf, int index, int len) {
		crc.update(buf, index, len);
	}

	@Override
	public void reset() {
		crc.reset();
	}

	@Override
	public long getValue() {
		return crc.getValue();
	}

	@Override
	public void update(int b) {
		crc.update(new byte[] {(byte) b}, 0, 1);
	}
}
