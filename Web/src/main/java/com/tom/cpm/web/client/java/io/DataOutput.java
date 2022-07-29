package com.tom.cpm.web.client.java.io;

import java.io.IOException;

public interface DataOutput {
	public void write(byte[] data) throws IOException;
	public void write(byte[] data, int ofs, int len) throws IOException;
	public void write(int v) throws IOException;
	public void writeBoolean(boolean v) throws IOException;
	public void writeByte(int v) throws IOException;
	public void writeBytes(String s) throws IOException;
	public void writeChar(int v) throws IOException;
	public void writeChars(String s) throws IOException;
	public void writeDouble(double v) throws IOException;
	public void writeFloat(float v) throws IOException;
	public void writeInt(int v) throws IOException;
	public void writeLong(long v) throws IOException;
	public void writeShort(int v) throws IOException;
	public void writeUTF(String s) throws IOException;
}
