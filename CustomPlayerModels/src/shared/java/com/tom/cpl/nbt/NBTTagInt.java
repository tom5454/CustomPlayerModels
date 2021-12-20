package com.tom.cpl.nbt;

import java.io.IOException;

import com.tom.cpl.nbt.NBTTag.NBTPrimitive;
import com.tom.cpm.shared.io.IOHelper;

public class NBTTagInt extends NBTPrimitive {

	private int data;

	NBTTagInt() {
	}

	public NBTTagInt(int data) {
		this.data = data;
	}

	@Override
	public void write(IOHelper output) throws IOException {
		output.writeInt(this.data);
	}

	@Override
	public void read(IOHelper input) throws IOException {
		this.data = input.readInt();
	}

	@Override
	public byte getId() {
		return TAG_INT;
	}

	@Override
	public String toString() {
		return String.valueOf(this.data);
	}

	@Override
	public NBTTagInt copy() {
		return new NBTTagInt(this.data);
	}

	@Override
	public boolean equals(Object other) {
		if (super.equals(other)) {
			NBTTagInt nbttagint = (NBTTagInt)other;
			return this.data == nbttagint.data;
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
		return (short)(this.data & 65535);
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
