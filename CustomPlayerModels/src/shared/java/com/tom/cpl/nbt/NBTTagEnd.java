package com.tom.cpl.nbt;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;

public class NBTTagEnd extends NBTTag {

	@Override
	public byte getId() {
		return TAG_END;
	}

	@Override
	public void write(IOHelper o) throws IOException {
	}

	@Override
	public void read(IOHelper i) throws IOException {
	}

	@Override
	public NBTTag copy() {
		return new NBTTagEnd();
	}
}
