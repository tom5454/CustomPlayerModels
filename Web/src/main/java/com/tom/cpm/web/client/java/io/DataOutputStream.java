/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.tom.cpm.web.client.java.io;

import java.io.IOException;
import java.io.OutputStream;

import com.tom.cpm.web.client.java.nio.Numbers;

public class DataOutputStream extends OutputStream implements DataOutput {

	OutputStream os;

	public DataOutputStream (OutputStream os) {
		this.os = os;
	}

	@Override
	public void write(int b) throws IOException {
		os.write(b);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		os.write(v ? 1 : 0);
	}

	@Override
	public void writeByte(int v) throws IOException {
		os.write(v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		int len = s.length();
		for(int i = 0; i < len; i++) {
			os.write(s.charAt(i) & 0xff);
		}
	}

	@Override
	public void writeChar(int v) throws IOException {
		os.write(v >> 8);
		os.write(v);
	}

	@Override
	public void writeChars(String s) throws IOException {
		throw new RuntimeException("writeChars NYI");
	}

	@Override
	public void writeDouble(double v) throws IOException {
		throw new RuntimeException("writeDouble");
	}

	@Override
	public void writeFloat(float v) throws IOException {
		writeInt(Numbers.floatToIntBits(v));
	}

	@Override
	public void writeInt(int v) throws IOException {
		os.write(v >> 24);
		os.write(v >> 16);
		os.write(v >> 8);
		os.write(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		writeInt((int) (v >> 32L));
		writeInt((int) v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		os.write(v >> 8);
		os.write(v);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		/*ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 0 && c < 80) {
				baos.write(c);
			} else if (c < '\u0800') {
				baos.write(0xc0 | (0x1f & (c >> 6)));
				baos.write(0x80 | (0x3f & c));
			} else {
				baos.write(0xe0 | (0x0f & (c >> 12)));
				baos.write(0x80 | (0x3f & (c >>  6)));
				baos.write(0x80 | (0x3f & c));
			}
		}
		writeShort(baos.count);
		os.write(baos.buf, 0, baos.count);*/
	}
}