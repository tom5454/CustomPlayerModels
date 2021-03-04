package com.tom.cpm.shared.template.args;

import java.io.EOFException;
import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;

public class ColorArg extends ArgBase {
	private int color;

	public ColorArg() {
	}

	public ColorArg(String name, int color) {
		super(name);
		this.color = color;
	}

	@Override
	public String getType() {
		return "color";
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.write((color >>> 16) & 0xFF);
		dout.write((color >>>  8) & 0xFF);
		dout.write((color >>>  0) & 0xFF);
	}

	@Override
	public void load(IOHelper din) throws IOException {
		int ch2 = din.read();
		int ch3 = din.read();
		int ch4 = din.read();
		if ((ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		color = ((0xff << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	@Override
	public Object toJson() {
		return Integer.toHexString(color);
	}

	@Override
	public void fromJson(Object v) {
		color = Integer.parseUnsignedInt((String) v, 16);
	}

	public int getColor() {
		return color;
	}

	public void setColor(int value) {
		this.color = value;
	}
}
