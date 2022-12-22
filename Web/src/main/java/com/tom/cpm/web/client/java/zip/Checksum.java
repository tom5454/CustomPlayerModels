package com.tom.cpm.web.client.java.zip;

public interface Checksum {
	public void update(int b);
	public void update(byte[] b, int off, int len);
	public long getValue();
	public void reset();
}
