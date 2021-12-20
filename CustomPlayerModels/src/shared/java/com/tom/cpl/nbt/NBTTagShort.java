package com.tom.cpl.nbt;

import java.io.IOException;

import com.tom.cpl.nbt.NBTTag.NBTPrimitive;
import com.tom.cpm.shared.io.IOHelper;

public class NBTTagShort extends NBTPrimitive {

	private short data;

	public NBTTagShort() {
	}

	public NBTTagShort(short data) {
		this.data = data;
	}

	@Override
	public void write(IOHelper output) throws IOException {
		output.writeShort(this.data);
	}

	@Override
	public void read(IOHelper input) throws IOException {
		this.data = input.readShort();
	}

	@Override
	public byte getId() {
		return TAG_SHORT;
	}

	@Override
	public String toString() {
		return this.data + "s";
	}

	@Override
	public NBTTag copy() {
		return new NBTTagShort(this.data);
	}

	@Override
	public boolean equals(Object other) {
		if (super.equals(other)) {
			NBTTagShort nbttagshort = (NBTTagShort)other;
			return this.data == nbttagshort.data;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ this.data;
	}

	@Override
	public long getLong() {
		return this.data;
	}

	@Override
	public int getInt() {
		return this.data;
	}

	@Override
	public short getShort() {
		return this.data;
	}

	@Override
	public byte getByte() {
		return (byte)(this.data & 255);
	}

	@Override
	public double getDouble() {
		return this.data;
	}

	@Override
	public float getFloat() {
		return this.data;
	}
}
