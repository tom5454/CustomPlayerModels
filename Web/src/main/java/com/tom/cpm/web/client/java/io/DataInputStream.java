package com.tom.cpm.web.client.java.io;

import java.io.IOException;
import java.io.InputStream;

import com.tom.cpm.web.client.java.nio.Numbers;

public class DataInputStream extends InputStream implements DataInput {

	private final InputStream is;

	public DataInputStream(final InputStream is) {
		this.is = is;
	}

	@Override
	public int read() throws IOException {
		return is.read();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return readByte() != 0;
	}

	@Override
	public byte readByte() throws IOException {
		int i = read();
		if (i == -1) {
			throw new EOFException();
		}
		return (byte) i;
	}

	@Override
	public char readChar() throws IOException {
		int a = is.read();
		int b = readUnsignedByte();
		return (char) ((a << 8) | b);
	}

	@Override
	public double readDouble() throws IOException {
		throw new RuntimeException("readDouble");
	}

	@Override
	public float readFloat() throws IOException {
		return Numbers.intBitsToFloat(readInt());
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		while (len > 0) {
			int count = is.read(b, off, len);
			if (count <= 0) {
				throw new EOFException();
			}
			off += count;
			len -= count;
		}
	}

	@Override
	public int readInt() throws IOException {
		int a = is.read();
		int b = is.read();
		int c = is.read();
		int d = readUnsignedByte();
		return (a << 24) | (b << 16) | (c << 8) | d;
	}

	@Override
	public String readLine() throws IOException {
		throw new RuntimeException("readline NYI");
	}

	private byte readBuffer[] = new byte[8];

	@Override
	public long readLong() throws IOException {
		readFully(readBuffer, 0, 8);
		return (((long)readBuffer[0] << 56) +
				((long)(readBuffer[1] & 255) << 48) +
				((long)(readBuffer[2] & 255) << 40) +
				((long)(readBuffer[3] & 255) << 32) +
				((long)(readBuffer[4] & 255) << 24) +
				((readBuffer[5] & 255) << 16) +
				((readBuffer[6] & 255) <<  8) +
				((readBuffer[7] & 255) <<  0));
	}

	@Override
	public short readShort() throws IOException {
		int a = is.read();
		int b = readUnsignedByte();
		return (short) ((a << 8) | b);
	}

	@Override
	public String readUTF() throws IOException {
		int bytes = readUnsignedShort();
		StringBuilder sb = new StringBuilder();

		while (bytes > 0) {
			bytes -= readUtfChar(sb);
		}

		return sb.toString();
	}

	private int readUtfChar(StringBuilder sb) throws IOException {
		int a = readUnsignedByte();
		if ((a & 0x80) == 0) {
			sb.append((char) a);
			return 1;
		}
		if ((a & 0xe0) == 0xb0) {
			int b = readUnsignedByte();
			sb.append((char)(((a& 0x1F) << 6) | (b & 0x3F)));
			return 2;
		}
		if ((a & 0xf0) == 0xe0) {
			int b = is.read();
			int c = readUnsignedByte();
			sb.append((char)(((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F)));
			return 3;
		}
		throw new UTFDataFormatException();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		int i = read();
		if (i == -1) {
			throw new EOFException();
		}
		return i;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		int a = is.read();
		int b = readUnsignedByte();
		return ((a << 8) | b);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		// note: This is actually a valid implementation of this method, rendering it quite useless...
		return 0;
	}

	@Override
	public void reset() throws IOException {
		is.reset();
	}
}
