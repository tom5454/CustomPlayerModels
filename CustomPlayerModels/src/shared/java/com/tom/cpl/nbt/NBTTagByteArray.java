package com.tom.cpl.nbt;

import java.io.IOException;
import java.util.Arrays;

import com.tom.cpm.shared.io.IOHelper;

public class NBTTagByteArray extends NBTTag {

	private byte[] data;

	NBTTagByteArray() {
	}

	public NBTTagByteArray(byte[] data) {
		this.data = data;
	}

	@Override
	public void write(IOHelper output) throws IOException {
		output.writeInt(this.data.length);
		output.write(this.data);
	}

	@Override
	public void read(IOHelper input) throws IOException {
		int i = input.readInt();
		this.data = new byte[i];
		input.readFully(this.data);
	}

	@Override
	public byte getId() {
		return TAG_BYTE_ARRAY;
	}

	@Override
	public String toString() {
		return "[" + this.data.length + " bytes]";
	}

	@Override
	public NBTTag copy() {
		byte[] abyte = new byte[this.data.length];
		System.arraycopy(this.data, 0, abyte, 0, this.data.length);
		return new NBTTagByteArray(abyte);
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other) ? Arrays.equals(this.data, ((NBTTagByteArray)other).data) : false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ Arrays.hashCode(this.data);
	}

	public byte[] getByteArray() {
		return this.data;
	}
}
