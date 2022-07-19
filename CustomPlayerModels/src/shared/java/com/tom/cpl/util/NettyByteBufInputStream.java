package com.tom.cpl.util;

import java.io.IOException;
import java.io.InputStream;

import io.netty.buffer.ByteBuf;

public class NettyByteBufInputStream extends InputStream {
	private int ptr;
	private int end;
	private ByteBuf buf;

	public NettyByteBufInputStream(ByteBuf buf) {
		this.buf = buf;
		ptr = buf.readerIndex();
		end = ptr + buf.readableBytes();
	}

	@Override
	public int read() throws IOException {
		if (!buf.isReadable()) {
			return -1;
		}
		if(ptr < end) {
			int p = ptr++;
			return buf.getByte(p) & 0xff;
		} else
			return -1;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (!buf.isReadable()) {
			return -1;
		}
		if(ptr < end) {
			len = Math.min(end - ptr, len);
			buf.getBytes(ptr, b, off, len);
			ptr += len;
			return len;
		} else
			return -1;
	}

	@Override
	public int available() throws IOException {
		return end - ptr;
	}
}
